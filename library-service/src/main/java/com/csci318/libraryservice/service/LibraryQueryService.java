package com.csci318.libraryservice.service;

import com.csci318.libraryservice.domain.aggregateroot.Loan;
import com.csci318.libraryservice.domain.entity.Library;
import com.csci318.libraryservice.domain.valueobject.CustomerId;
import com.csci318.libraryservice.domain.valueobject.LibraryId;
import com.csci318.libraryservice.dto.BookAvailabilityResponse;
import com.csci318.libraryservice.dto.LibraryAvailabilityResponse;
import com.csci318.libraryservice.dto.LoanResponse;
import com.csci318.libraryservice.exception.ResourceNotFoundException;
import com.csci318.libraryservice.exception.ValidationFailedException;
import com.csci318.libraryservice.persistence.entity.InventoryReadModelEntity;
import com.csci318.libraryservice.persistence.repository.CustomerRepository;
import com.csci318.libraryservice.persistence.repository.InventoryReadModelRepository;
import com.csci318.libraryservice.persistence.repository.LibraryRepository;
import com.csci318.libraryservice.persistence.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LibraryQueryService {

    private final CustomerRepository customerRepository;
    private final InventoryReadModelRepository inventoryReadModelRepository;
    private final LibraryRepository libraryRepository;
    private final LoanRepository loanRepository;
    private final Clock clock = Clock.systemDefaultZone();

    public LibraryQueryService(CustomerRepository customerRepository,
                               InventoryReadModelRepository inventoryReadModelRepository,
                               LibraryRepository libraryRepository,
                               LoanRepository loanRepository) {
        this.customerRepository = customerRepository;
        this.inventoryReadModelRepository = inventoryReadModelRepository;
        this.libraryRepository = libraryRepository;
        this.loanRepository = loanRepository;
    }

    public List<LibraryAvailabilityResponse> findLibrariesByIsbn(String isbn) {
        List<InventoryReadModelEntity> inventories = inventoryReadModelRepository.findByIsbnAndAvailableCopiesGreaterThan(isbn, 0);
        if (inventories.isEmpty()) {
            throw new ResourceNotFoundException("No library has available copies for ISBN " + isbn);
        }
        return inventories.stream().map(inventory -> {
            Library library = libraryRepository.findById(LibraryId.of(inventory.getLibraryId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Library " + inventory.getLibraryId() + " was not found"));
            return new LibraryAvailabilityResponse(
                    library.getId().value(),
                    library.getName(),
                    library.getLocation(),
                    inventory.getAvailableCopies(),
                    inventory.getTotalCopies());
        }).toList();
    }

    public BookAvailabilityResponse checkBookAvailability(String libraryId, String isbn) {
        InventoryReadModelEntity inventory = inventoryReadModelRepository.findByLibraryIdAndIsbn(libraryId, isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book inventory not found for library " + libraryId + " and ISBN " + isbn));
        return new BookAvailabilityResponse(
                inventory.getLibraryId(),
                inventory.getIsbn(),
                inventory.getAvailableCopies(),
                inventory.getTotalCopies(),
                inventory.getAvailableCopies() > 0);
    }

    public List<LoanResponse> listActiveLoansForCustomer(String customerId, String status) {
        CustomerId customer = CustomerId.of(customerId);
        if (!customerRepository.existsById(customer)) {
            throw new ResourceNotFoundException("Customer " + customerId + " was not found");
        }
        if (status != null && !"ACTIVE".equalsIgnoreCase(status)) {
            throw new ValidationFailedException("Unsupported status filter: " + status);
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return loanRepository.findByCustomerId(customer).stream()
                .map(loan -> toLoanResponse(loan, now))
                .filter(loan -> "BORROWED".equals(loan.status()) || "OVERDUE".equals(loan.status()))
                .toList();
    }

    public List<LoanResponse> listOverdueLoansByLibrary(String libraryId, String overdueBucket) {
        LibraryId library = LibraryId.of(libraryId);
        if (!libraryRepository.existsById(library)) {
            throw new ResourceNotFoundException("Library " + libraryId + " was not found");
        }
        Integer minimumOverdueDays = parseOverdueBucket(overdueBucket);
        LocalDateTime now = LocalDateTime.now(clock);
        List<LoanResponse> results = loanRepository.findByLibraryId(library).stream()
                .map(loan -> toLoanResponse(loan, now))
                .filter(loan -> "OVERDUE".equals(loan.status()))
                .toList();
        if (minimumOverdueDays == null) {
            return results;
        }
        return results.stream()
                .filter(loan -> ChronoUnit.DAYS.between(loan.dueDate(), now) > minimumOverdueDays)
                .toList();
    }

    private Integer parseOverdueBucket(String overdueBucket) {
        if (overdueBucket == null || overdueBucket.isBlank()) {
            return null;
        }
        if (!overdueBucket.matches("gt\\d+d")) {
            throw new ValidationFailedException("Unsupported overdueBucket value: " + overdueBucket);
        }
        return Integer.parseInt(overdueBucket.substring(2, overdueBucket.length() - 1));
    }

    private LoanResponse toLoanResponse(Loan loan, LocalDateTime now) {
        String status = loan.statusAt(now).name();
        return new LoanResponse(
                loan.getLoanId(),
                loan.getLibraryId().value(),
                loan.getIsbn().value(),
                loan.getCustomerId().value(),
                loan.getLoanPeriod().getBorrowedAt(),
                loan.getLoanPeriod().getDueDate(),
                loan.getReturnedAt(),
                loan.getLoanPeriod().getRenewalCount(),
                status);
    }
}
