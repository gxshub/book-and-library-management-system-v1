package com.csci318.libraryservice.dto;

public record LibraryAvailabilityResponse(
        String libraryId,
        String libraryName,
        String location,
        int availableCopies,
        int totalCopies) {
}
