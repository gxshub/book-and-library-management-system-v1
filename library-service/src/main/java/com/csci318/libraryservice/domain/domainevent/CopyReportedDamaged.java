package com.csci318.libraryservice.domain.domainevent;

import com.csci318.libraryservice.domain.valueobject.CustomerId;
import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LibraryId;

import java.time.LocalDateTime;

public record CopyReportedDamaged(String loanId, LibraryId libraryId, Isbn isbn, CustomerId customerId, LocalDateTime occurredAt) {
}
