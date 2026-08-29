package com.csci318.libraryservice.service;

import com.csci318.libraryservice.client.BookServiceClient;
import com.csci318.libraryservice.domain.aggregateroot.FineLedger;
import com.csci318.libraryservice.domain.aggregateroot.Loan;
import com.csci318.libraryservice.domain.domainservice.CirculationPolicyService;
import com.csci318.libraryservice.domain.domainservice.FinePolicyService;
import com.csci318.libraryservice.domain.valueobject.CustomerId;
import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LibraryId;
import com.csci318.libraryservice.domain.valueobject.Money;
import com.csci318.libraryservice.domain.valueobject.PolicySnapshot;
import com.csci318.libraryservice.dto.BorrowBookRequest;
import com.csci318.libraryservice.dto.FinePaymentRequest;
import com.csci318.libraryservice.dto.FineResponse;
import com.csci318.libraryservice.dto.InventoryAdjustmentRequest;
import com.csci318.libraryservice.dto.InventoryAdjustmentResponse;
import com.csci318.libraryservice.dto.InventoryTransferRequest;
import com.csci318.libraryservice.dto.InventoryTransferResponse;
import com.csci318.libraryservice.dto.LoanResponse;
import com.csci318.libraryservice.dto.LossDamageReportRequest;
import com.csci318.libraryservice.dto.MoneyDto;
import com.csci318.libraryservice.dto.RenewLoanRequest;
import com.csci318.libraryservice.dto.ReturnBookRequest;
import com.csci318.libraryservice.exception.LoanPolicyViolationException;
import com.csci318.libraryservice.exception.ResourceNotFoundException;
import com.csci318.libraryservice.exception.ValidationFailedException;
import com.csci318.libraryservice.persistence.repository.CustomerRepository;
import com.csci318.libraryservice.persistence.repository.FineLedgerRepository;
import com.csci318.libraryservice.persistence.repository.LibraryPolicyRepository;
import com.csci318.libraryservice.persistence.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class LibraryCommandService {

    private final BookServiceClient bookServiceClient;
    private final CustomerRepository customerRepository;
    private final FineLedgerRepository fineLedgerRepository;
    private final InventoryEventSourcingService inventoryEventSourcingService;
    private final LibraryPolicyRepository libraryPolicyRepository;
    private final LoanRepository loanRepository;
    private final CirculationPolicyService circulationPolicyService;
    private final FinePolicyService finePolicyService;
    private final Clock clock = Clock.systemDefaultZone();

    public LibraryCommandService(BookServiceClient bookServiceClient,
                                 CustomerRepository customerRepository,
                                 FineLedgerRepository fineLedgerRepository,
                                 InventoryEventSourcingService inventoryEventSourcingService,
                                 LibraryPolicyRepository libraryPolicyRepository,
                                 LoanRepository loanRepository,
                                 CirculationPolicyService circulationPolicyService,
                                 FinePolicyService finePolicyService) {
        this.bookServiceClient = bookServiceClient;
        this.customerRepository = customerRepository;
        this.fineLedgerRepository = fineLedgerRepository;
        this.inventoryEventSourcingService = inventoryEventSourcingService;
        this.libraryPolicyRepository = libraryPolicyRepository;
        this.loanRepository = loanRepository;
        this.circulationPolicyService = circulationPolicyService;
        this.finePolicyService = finePolicyService;
    }

    public LoanResponse borrowBook(String libraryId, String isbn, BorrowBookRequest request) {
        CustomerId customerId = CustomerId.of(request.customerId());
        LibraryId library = LibraryId.of(libraryId);
        Isbn bookIsbn = Isbn.of(isbn);
        requireCustomer(customerId);
        bookServiceClient.validateBookExists(isbn);

        PolicySnapshot policy = circulationPolicyService.captureSnapshot(library);
        LocalDateTime now = LocalDateTime.now(clock);
        String loanId = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        inventoryEventSourcingService.reserveCopy(libraryId, isbn, loanId);

        Loan loan = Loan.borrow(loanId, library, bookIsbn, customerId, policy, now,
                circulationPolicyService.calculateDueDate(now, policy));
        loan.borrowEvent(now);
        return toLoanResponse(loanRepository.save(loan), now);
    }

    public LoanResponse returnBook(String libraryId, String isbn, String loanId, ReturnBookRequest request) {
        CustomerId customerId = CustomerId.of(request.customerId());
        Loan loan = loanRepository.findByLoanIdAndLibraryIdAndIsbnAndCustomerId(
                        loanId, LibraryId.of(libraryId), Isbn.of(isbn), customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan " + loanId + " was not found"));
        LocalDateTime now = LocalDateTime.now(clock);
        Loan.LoanStatus effectiveStatus = loan.statusAt(now);
        if (effectiveStatus != Loan.LoanStatus.BORROWED && effectiveStatus != Loan.LoanStatus.OVERDUE) {
            throw new LoanPolicyViolationException("Only active loans can be returned");
        }

        loan.markReturned(now);
        inventoryEventSourcingService.releaseCopy(libraryId, isbn, loanId);
        Money overdueFine = finePolicyService.calculateOverdueFine(loan, now);
        if (overdueFine.amount().compareTo(BigDecimal.ZERO) > 0) {
            assessFine(loan, overdueFine);
        }
        return toLoanResponse(loanRepository.save(loan), now);
    }

    public LoanResponse renewLoan(String libraryId, String isbn, String loanId, RenewLoanRequest request) {
        Loan loan = loanRepository.findByLoanIdAndLibraryIdAndIsbn(loanId, LibraryId.of(libraryId), Isbn.of(isbn))
                .orElseThrow(() -> new ResourceNotFoundException("Loan " + loanId + " was not found"));
        LocalDateTime now = LocalDateTime.now(clock);
        if (!circulationPolicyService.isRenewalAllowed(loan, now)) {
            throw new LoanPolicyViolationException("Loan cannot be renewed under the current policy");
        }
        loan.renew(now, circulationPolicyService.calculateDueDate(now, loan.getPolicySnapshot()));
        return toLoanResponse(loanRepository.save(loan), now);
    }

    public LoanResponse reportLossOrDamage(String libraryId, String isbn, String loanId, LossDamageReportRequest request) {
        CustomerId customerId = CustomerId.of(request.customerId());
        requireCustomer(customerId);
        Loan loan = loanRepository.findByLoanIdAndLibraryIdAndIsbnAndCustomerId(
                        loanId, LibraryId.of(libraryId), Isbn.of(isbn), customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan " + loanId + " was not found"));
        LocalDateTime now = LocalDateTime.now(clock);
        Loan.LoanStatus effectiveStatus = loan.statusAt(now);
        if (effectiveStatus != Loan.LoanStatus.BORROWED && effectiveStatus != Loan.LoanStatus.OVERDUE) {
            throw new LoanPolicyViolationException("Only active loans can be reported as lost or damaged");
        }

        String reasonCode = request.reportType().name() + "_REPORTED";
        inventoryEventSourcingService.applyLossOrDamage(libraryId, isbn, reasonCode, request.customerId());
        if (request.reportType() == LossDamageReportRequest.ReportType.LOST) {
            loan.reportLost(now);
            assessFine(loan, finePolicyService.calculateLostCharge(loan.getPolicySnapshot()));
        } else {
            loan.reportDamaged(now);
            assessFine(loan, finePolicyService.calculateDamagedCharge(loan.getPolicySnapshot()));
        }
        return toLoanResponse(loanRepository.save(loan), now);
    }

    public InventoryAdjustmentResponse adjustInventoryStock(String libraryId, String isbn, InventoryAdjustmentRequest request) {
        var state = inventoryEventSourcingService.adjustInventory(libraryId, isbn, request);
        return new InventoryAdjustmentResponse(
                state.getInventoryId(),
                state.getLibraryId(),
                state.getIsbn(),
                state.getTotalCopies(),
                state.getAvailableCopies(),
                state.getVersion());
    }

    public InventoryTransferResponse transferInventory(InventoryTransferRequest request) {
        InventoryEventSourcingService.InventoryTransferResult result = inventoryEventSourcingService.transferInventory(request);
        return new InventoryTransferResponse(result.transferId(), result.status());
    }

    public FineResponse payFine(String fineId, FinePaymentRequest request) {
        FineLedger fineLedger = fineLedgerRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine " + fineId + " was not found"));
        if (!fineLedger.getAssessed().currency().equalsIgnoreCase(request.amount().currency())) {
            throw new ValidationFailedException("Payment currency does not match the fine currency");
        }
        fineLedger.recordPayment(new Money(request.amount().amount(), request.amount().currency()), LocalDateTime.now(clock));
        return toFineResponse(fineLedgerRepository.save(fineLedger));
    }

    private void requireCustomer(CustomerId customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer " + customerId.value() + " was not found");
        }
    }

    private void assessFine(Loan loan, Money amount) {
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        FineLedger fineLedger = fineLedgerRepository.findByLoanId(loan.getLoanId())
                .orElseGet(() -> new FineLedger(
                        "FINE-" + loan.getLoanId(),
                        loan.getLoanId(),
                        loan.getCustomerId(),
                        Money.zero(amount.currency()),
                        Money.zero(amount.currency()),
                        FineLedger.FineStatus.OPEN));
        fineLedger.assess(amount, "ASSESSMENT", LocalDateTime.now(clock));
        fineLedgerRepository.save(fineLedger);
    }

    private LoanResponse toLoanResponse(Loan loan, LocalDateTime now) {
        return new LoanResponse(
                loan.getLoanId(),
                loan.getLibraryId().value(),
                loan.getIsbn().value(),
                loan.getCustomerId().value(),
                loan.getLoanPeriod().getBorrowedAt(),
                loan.getLoanPeriod().getDueDate(),
                loan.getReturnedAt(),
                loan.getLoanPeriod().getRenewalCount(),
                loan.statusAt(now).name());
    }

    private FineResponse toFineResponse(FineLedger fineLedger) {
        return new FineResponse(
                fineLedger.getFineId(),
                fineLedger.getLoanId(),
                fineLedger.getCustomerId().value(),
                new MoneyDto(fineLedger.getAssessed().amount(), fineLedger.getAssessed().currency()),
                new MoneyDto(fineLedger.getPaid().amount(), fineLedger.getPaid().currency()),
                new MoneyDto(fineLedger.balance().amount(), fineLedger.balance().currency()),
                fineLedger.getStatus().name());
    }
}
