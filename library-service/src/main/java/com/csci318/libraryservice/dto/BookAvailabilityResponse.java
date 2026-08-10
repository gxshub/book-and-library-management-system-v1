package com.csci318.libraryservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookAvailabilityResponse {

    private String libraryId;
    private String isbn;
    private int availableCopies;
    private int totalCopies;

    @JsonProperty("isAvailable")
    private boolean isAvailable;

    public BookAvailabilityResponse() {
    }

    public BookAvailabilityResponse(String libraryId, String isbn, int availableCopies, int totalCopies, boolean isAvailable) {
        this.libraryId = libraryId;
        this.isbn = isbn;
        this.availableCopies = availableCopies;
        this.totalCopies = totalCopies;
        this.isAvailable = isAvailable;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    @JsonProperty("isAvailable")
    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
