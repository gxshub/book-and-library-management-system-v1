package com.csci318.libraryservice.dto;

public record BookAvailabilityResponse(
        String libraryId,
        String isbn,
        int availableCopies,
        int totalCopies,
        boolean isAvailable) {
}
