package com.csci318.libraryservice.domain.domainevent;

import com.csci318.libraryservice.domain.valueobject.CustomerId;
import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LibraryId;

import java.time.LocalDateTime;

public record LoanOverdueFlagged(String loanId, CustomerId customerId, LibraryId libraryId, Isbn isbn, LocalDateTime dueDate, LocalDateTime occurredAt) {
}
