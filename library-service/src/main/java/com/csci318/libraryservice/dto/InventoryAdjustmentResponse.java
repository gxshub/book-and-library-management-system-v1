package com.csci318.libraryservice.dto;

public record InventoryAdjustmentResponse(
        String inventoryId,
        String libraryId,
        String isbn,
        int totalCopies,
        int availableCopies,
        long version) {
}
