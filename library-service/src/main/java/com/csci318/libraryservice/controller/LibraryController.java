package com.csci318.libraryservice.controller;

import com.csci318.libraryservice.dto.BookAvailabilityResponse;
import com.csci318.libraryservice.dto.BorrowBookRequest;
import com.csci318.libraryservice.dto.FinePaymentRequest;
import com.csci318.libraryservice.dto.FineResponse;
import com.csci318.libraryservice.dto.InventoryAdjustmentRequest;
import com.csci318.libraryservice.dto.InventoryAdjustmentResponse;
import com.csci318.libraryservice.dto.InventoryTransferRequest;
import com.csci318.libraryservice.dto.InventoryTransferResponse;
import com.csci318.libraryservice.dto.LibraryAvailabilityResponse;
import com.csci318.libraryservice.dto.LoanResponse;
import com.csci318.libraryservice.dto.LossDamageReportRequest;
import com.csci318.libraryservice.dto.RenewLoanRequest;
import com.csci318.libraryservice.dto.ReturnBookRequest;
import com.csci318.libraryservice.service.LibraryCommandService;
import com.csci318.libraryservice.service.LibraryQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class LibraryController {

    private final LibraryCommandService libraryCommandService;
    private final LibraryQueryService libraryQueryService;

    public LibraryController(LibraryCommandService libraryCommandService, LibraryQueryService libraryQueryService) {
        this.libraryCommandService = libraryCommandService;
        this.libraryQueryService = libraryQueryService;
    }

    @GetMapping("/libraries/by-isbn/{isbn}")
    public ResponseEntity<List<LibraryAvailabilityResponse>> findLibrariesByIsbn(@PathVariable String isbn) {
        List<LibraryAvailabilityResponse> response = libraryQueryService.findLibrariesByIsbn(isbn);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/libraries/{libraryId}/books/{isbn}/availability")
    public ResponseEntity<BookAvailabilityResponse> checkBookAvailability(
            @PathVariable String libraryId,
            @PathVariable String isbn) {
        BookAvailabilityResponse response = libraryQueryService.checkBookAvailability(libraryId, isbn);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/libraries/{libraryId}/books/{isbn}/borrow")
    public ResponseEntity<LoanResponse> borrowBook(
            @PathVariable String libraryId,
            @PathVariable String isbn,
            @Valid @RequestBody BorrowBookRequest request) {
        LoanResponse response = libraryCommandService.borrowBook(libraryId, isbn, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/libraries/{libraryId}/books/{isbn}/loans/{loanId}/return")
    public ResponseEntity<LoanResponse> returnBook(
            @PathVariable String libraryId,
            @PathVariable String isbn,
            @PathVariable String loanId,
            @Valid @RequestBody ReturnBookRequest request) {
        LoanResponse response = libraryCommandService.returnBook(libraryId, isbn, loanId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/libraries/{libraryId}/books/{isbn}/loans/{loanId}/renew")
    public ResponseEntity<LoanResponse> renewLoan(
            @PathVariable String libraryId,
            @PathVariable String isbn,
            @PathVariable String loanId,
            @Valid @RequestBody RenewLoanRequest request) {
        LoanResponse response = libraryCommandService.renewLoan(libraryId, isbn, loanId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{customerId}/loans")
    public ResponseEntity<List<LoanResponse>> listActiveLoansForCustomer(
            @PathVariable String customerId,
            @RequestParam(name = "status", required = false, defaultValue = "ACTIVE") String status) {
        List<LoanResponse> response = libraryQueryService.listActiveLoansForCustomer(customerId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/libraries/{libraryId}/loans/overdue")
    public ResponseEntity<List<LoanResponse>> listOverdueLoansByLibrary(
            @PathVariable String libraryId,
            @RequestParam(name = "overdueBucket", required = false) String overdueBucket) {
        List<LoanResponse> response = libraryQueryService.listOverdueLoansByLibrary(libraryId, overdueBucket);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/libraries/{libraryId}/books/{isbn}/loans/{loanId}/report-loss-or-damage")
    public ResponseEntity<LoanResponse> reportLossOrDamage(
            @PathVariable String libraryId,
            @PathVariable String isbn,
            @PathVariable String loanId,
            @Valid @RequestBody LossDamageReportRequest request) {
        LoanResponse response = libraryCommandService.reportLossOrDamage(libraryId, isbn, loanId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/libraries/{libraryId}/books/{isbn}/inventory/adjust")
    public ResponseEntity<InventoryAdjustmentResponse> adjustInventoryStock(
            @PathVariable String libraryId,
            @PathVariable String isbn,
            @Valid @RequestBody InventoryAdjustmentRequest request) {
        InventoryAdjustmentResponse response = libraryCommandService.adjustInventoryStock(libraryId, isbn, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/inventory/transfers")
    public ResponseEntity<InventoryTransferResponse> transferInventory(
            @Valid @RequestBody InventoryTransferRequest request) {
        InventoryTransferResponse response = libraryCommandService.transferInventory(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/fines/{fineId}/payments")
    public ResponseEntity<FineResponse> payFine(
            @PathVariable String fineId,
            @Valid @RequestBody FinePaymentRequest request) {
        FineResponse response = libraryCommandService.payFine(fineId, request);
        return ResponseEntity.ok(response);
    }
}
