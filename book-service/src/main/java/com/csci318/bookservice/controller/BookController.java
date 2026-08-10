package com.csci318.bookservice.controller;

import com.csci318.bookservice.dto.BookCreateRequest;
import com.csci318.bookservice.dto.BookResponse;
import com.csci318.bookservice.dto.BookUpdateRequest;
import com.csci318.bookservice.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        BookResponse response = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> listAllBooks() {
        List<BookResponse> response = bookService.listAllBooks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<BookResponse> getBookByIsbn(@PathVariable String isbn) {
        BookResponse response = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable String isbn,
            @Valid @RequestBody BookUpdateRequest request) {
        BookResponse response = bookService.updateBook(isbn, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        bookService.deleteBook(isbn);
        return ResponseEntity.noContent().build();
    }
}
