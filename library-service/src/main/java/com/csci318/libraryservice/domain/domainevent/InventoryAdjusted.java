package com.csci318.libraryservice.domain.domainevent;

import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LibraryId;

import java.time.LocalDateTime;

public record InventoryAdjusted(LibraryId libraryId, Isbn isbn, int delta, String reasonCode, String operatorId, LocalDateTime occurredAt) {
}
