package com.csci318.libraryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InventoryTransferRequest(
        @NotBlank(message = "ISBN is required") String isbn,
        @NotBlank(message = "Source library ID is required") String sourceLibraryId,
        @NotBlank(message = "Target library ID is required") String targetLibraryId,
        @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity,
        @NotBlank(message = "Requester is required") String requestedBy) {
}
