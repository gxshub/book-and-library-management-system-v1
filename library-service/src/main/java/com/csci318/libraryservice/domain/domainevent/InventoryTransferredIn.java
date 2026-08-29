package com.csci318.libraryservice.domain.domainevent;

import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LibraryId;

import java.time.LocalDateTime;

public record InventoryTransferredIn(String transferId, LibraryId sourceLibraryId, LibraryId targetLibraryId, Isbn isbn, int quantity, LocalDateTime occurredAt) {
}
