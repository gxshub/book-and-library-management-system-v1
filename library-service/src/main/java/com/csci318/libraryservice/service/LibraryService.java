package com.csci318.libraryservice.service;

import com.csci318.libraryservice.client.BookServiceClient;
import com.csci318.libraryservice.domain.AvailabilityLog;
import com.csci318.libraryservice.domain.BookInventory;
import com.csci318.libraryservice.domain.BorrowRecord;
import com.csci318.libraryservice.domain.Library;
import com.csci318.libraryservice.dto.*;
import com.csci318.libraryservice.exception.BookNotAvailableException;
import com.csci318.libraryservice.exception.ResourceNotFoundException;
import com.csci318.libraryservice.repository.AvailabilityLogRepository;
import com.csci318.libraryservice.repository.BookInventoryRepository;
import com.csci318.libraryservice.repository.BorrowRecordRepository;
import com.csci318.libraryservice.repository.LibraryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final BookInventoryRepository inventoryRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final AvailabilityLogRepository availabilityLogRepository;
    private final BookServiceClient bookServiceClient;

    public LibraryService(LibraryRepository libraryRepository,
                          BookInventoryRepository inventoryRepository,
                          BorrowRecordRepository borrowRecordRepository,
                          AvailabilityLogRepository availabilityLogRepository,
                          BookServiceClient bookServiceClient) {
        this.libraryRepository = libraryRepository;
        this.inventoryRepository = inventoryRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.availabilityLogRepository = availabilityLogRepository;
        this.bookServiceClient = bookServiceClient;
    }

    @Transactional(readOnly = true)
    public List<LibraryAvailabilityResponse> findLibrariesByIsbn(String isbn) {
        List<BookInventory> inventories = inventoryRepository.findByIsbnAndAvailableCopiesGreaterThan(isbn, 0);
        return inventories.stream().map(inv -> {
            Library lib = libraryRepository.findById(inv.getLibraryId()).orElse(null);
            String name = lib != null ? lib.getName() : inv.getLibraryId();
            String location = lib != null ? lib.getLocation() : "Unknown";
            return new LibraryAvailabilityResponse(inv.getLibraryId(), name, location, inv.getAvailableCopies(), inv.getTotalCopies());
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookAvailabilityResponse checkBookAvailability(String libraryId, String isbn) {
        BookInventory inv = inventoryRepository.findByLibraryIdAndIsbn(libraryId, isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book inventory not found for library " + libraryId + " and ISBN " + isbn));
        return new BookAvailabilityResponse(libraryId, isbn, inv.getAvailableCopies(), inv.getTotalCopies(), inv.getAvailableCopies() > 0);
    }

    public BorrowRecordResponse borrowBook(String libraryId, String isbn, BorrowBookRequest request) {
        // Step 1: Inter-Service Validation with Book Service (REST Call)
        bookServiceClient.validateBookExists(isbn);

        // Step 2: Check Local Inventory
        BookInventory inventory = inventoryRepository.findByLibraryIdAndIsbn(libraryId, isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book inventory not found in library " + libraryId));

        if (inventory.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("No copies available for book " + isbn + " in library " + libraryId);
        }

        // Step 3: Update Inventory & Log
        int prevCopies = inventory.getAvailableCopies();
        inventory.setAvailableCopies(prevCopies - 1);
        inventoryRepository.save(inventory);

        availabilityLogRepository.save(new AvailabilityLog(
                libraryId, isbn, prevCopies, inventory.getAvailableCopies(),
                AvailabilityLog.ChangeReason.BORROW, LocalDateTime.now()
        ));

        // Step 4: Create Borrow Record
        String recordId = "REC-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(14);

        BorrowRecord record = new BorrowRecord(
                recordId, libraryId, isbn, request.getCustomerId(),
                now, dueDate, BorrowRecord.BorrowStatus.BORROWED
        );
        BorrowRecord saved = borrowRecordRepository.save(record);

        return mapToBorrowResponse(saved);
    }

    public BorrowRecordResponse returnBook(String libraryId, String isbn, ReturnBookRequest request) {
        BorrowRecord record = borrowRecordRepository.findByIdAndLibraryIdAndIsbnAndCustomerIdAndStatus(
                request.getBorrowRecordId(), libraryId, isbn, request.getCustomerId(), BorrowRecord.BorrowStatus.BORROWED
        ).orElseThrow(() -> new ResourceNotFoundException("Active borrow record not found for record ID " + request.getBorrowRecordId()));

        BookInventory inventory = inventoryRepository.findByLibraryIdAndIsbn(libraryId, isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book inventory not found in library " + libraryId));

        // Update Borrow Record
        record.setReturnedAt(LocalDateTime.now());
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        BorrowRecord updatedRecord = borrowRecordRepository.save(record);

        // Update Inventory & Log
        int prevCopies = inventory.getAvailableCopies();
        inventory.setAvailableCopies(prevCopies + 1);
        inventoryRepository.save(inventory);

        availabilityLogRepository.save(new AvailabilityLog(
                libraryId, isbn, prevCopies, inventory.getAvailableCopies(),
                AvailabilityLog.ChangeReason.RETURN, LocalDateTime.now()
        ));

        return mapToBorrowResponse(updatedRecord);
    }

    private BorrowRecordResponse mapToBorrowResponse(BorrowRecord record) {
        return new BorrowRecordResponse(
                record.getId(), record.getLibraryId(), record.getIsbn(), record.getCustomerId(),
                record.getBorrowedAt(), record.getDueDate(), record.getReturnedAt(), record.getStatus().name()
        );
    }
}
