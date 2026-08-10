package com.csci318.libraryservice.dto;

public class LibraryAvailabilityResponse {

    private String libraryId;
    private String libraryName;
    private String location;
    private int availableCopies;
    private int totalCopies;

    public LibraryAvailabilityResponse() {
    }

    public LibraryAvailabilityResponse(String libraryId, String libraryName, String location, int availableCopies, int totalCopies) {
        this.libraryId = libraryId;
        this.libraryName = libraryName;
        this.location = location;
        this.availableCopies = availableCopies;
        this.totalCopies = totalCopies;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
}
