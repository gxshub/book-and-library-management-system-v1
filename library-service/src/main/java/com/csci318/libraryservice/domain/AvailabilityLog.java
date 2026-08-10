package com.csci318.libraryservice.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "availability_logs")
public class AvailabilityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String libraryId;
    private String isbn;
    private int previousAvailableCopies;
    private int newAvailableCopies;

    @Enumerated(EnumType.STRING)
    private ChangeReason changeReason;

    private LocalDateTime timestamp;

    public enum ChangeReason {
        BORROW, RETURN, STOCK_ADJUSTMENT
    }

    public AvailabilityLog() {
    }

    public AvailabilityLog(String libraryId, String isbn, int previousAvailableCopies, int newAvailableCopies, ChangeReason changeReason, LocalDateTime timestamp) {
        this.libraryId = libraryId;
        this.isbn = isbn;
        this.previousAvailableCopies = previousAvailableCopies;
        this.newAvailableCopies = newAvailableCopies;
        this.changeReason = changeReason;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getPreviousAvailableCopies() {
        return previousAvailableCopies;
    }

    public void setPreviousAvailableCopies(int previousAvailableCopies) {
        this.previousAvailableCopies = previousAvailableCopies;
    }

    public int getNewAvailableCopies() {
        return newAvailableCopies;
    }

    public void setNewAvailableCopies(int newAvailableCopies) {
        this.newAvailableCopies = newAvailableCopies;
    }

    public ChangeReason getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(ChangeReason changeReason) {
        this.changeReason = changeReason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
