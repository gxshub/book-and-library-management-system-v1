package com.csci318.libraryservice.domain.domainevent;

import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LibraryId;

import java.time.LocalDateTime;

public record InventoryCopyReserved(LibraryId libraryId, Isbn isbn, String loanId, LocalDateTime occurredAt) {
}
