package com.csci318.libraryservice.controller;

import com.csci318.libraryservice.dto.*;
import com.csci318.libraryservice.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/libraries/by-isbn/{isbn}")
    public ResponseEntity<List<LibraryAvailabilityResponse>> findLibrariesByIsbn(@PathVariable String isbn) {
        List<LibraryAvailabilityResponse> response = libraryService.findLibrariesByIsbn(isbn);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/libraries/{libraryId}/books/{isbn}/availability")
    public ResponseEntity<BookAvailabilityResponse> checkBookAvailability(
            @PathVariable String libraryId,
            @PathVariable String isbn) {
        BookAvailabilityResponse response = libraryService.checkBookAvailability(libraryId, isbn);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/libraries/{libraryId}/books/{isbn}/borrow")
    public ResponseEntity<BorrowRecordResponse> borrowBook(
            @PathVariable String libraryId,
            @PathVariable String isbn,
            @Valid @RequestBody BorrowBookRequest request) {
        BorrowRecordResponse response = libraryService.borrowBook(libraryId, isbn, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/libraries/{libraryId}/books/{isbn}/return")
    public ResponseEntity<BorrowRecordResponse> returnBook(
            @PathVariable String libraryId,
            @PathVariable String isbn,
            @Valid @RequestBody ReturnBookRequest request) {
        BorrowRecordResponse response = libraryService.returnBook(libraryId, isbn, request);
        return ResponseEntity.ok(response);
    }
}
