package com.csci318.bookservice.service;

import com.csci318.bookservice.domain.Book;
import com.csci318.bookservice.dto.BookCreateRequest;
import com.csci318.bookservice.dto.BookResponse;
import com.csci318.bookservice.dto.BookUpdateRequest;
import com.csci318.bookservice.exception.BookAlreadyExistsException;
import com.csci318.bookservice.exception.BookNotFoundException;
import com.csci318.bookservice.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse createBook(BookCreateRequest request) {
        if (bookRepository.existsById(request.getIsbn())) {
            throw new BookAlreadyExistsException("Book with ISBN " + request.getIsbn() + " already exists");
        }
        Book book = new Book(request.getIsbn(), request.getTitle(), request.getAuthor());
        Book saved = bookRepository.save(book);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book with ISBN " + isbn + " not found"));
        return mapToResponse(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> listAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookResponse updateBook(String isbn, BookUpdateRequest request) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book with ISBN " + isbn + " not found"));
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        Book updated = bookRepository.save(book);
        return mapToResponse(updated);
    }

    public void deleteBook(String isbn) {
        if (!bookRepository.existsById(isbn)) {
            throw new BookNotFoundException("Book with ISBN " + isbn + " not found");
        }
        bookRepository.deleteById(isbn);
    }

    private BookResponse mapToResponse(Book book) {
        return new BookResponse(book.getIsbn(), book.getTitle(), book.getAuthor());
    }
}
