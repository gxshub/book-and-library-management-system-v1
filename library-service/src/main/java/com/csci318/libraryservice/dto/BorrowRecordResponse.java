package com.csci318.libraryservice.dto;

import java.time.LocalDateTime;

public class BorrowRecordResponse {

    private String borrowRecordId;
    private String libraryId;
    private String isbn;
    private String customerId;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;
    private String status;

    public BorrowRecordResponse() {
    }

    public BorrowRecordResponse(String borrowRecordId, String libraryId, String isbn, String customerId, LocalDateTime borrowedAt, LocalDateTime dueDate, LocalDateTime returnedAt, String status) {
        this.borrowRecordId = borrowRecordId;
        this.libraryId = libraryId;
        this.isbn = isbn;
        this.customerId = customerId;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.returnedAt = returnedAt;
        this.status = status;
    }

    public String getBorrowRecordId() {
        return borrowRecordId;
    }

    public void setBorrowRecordId(String borrowRecordId) {
        this.borrowRecordId = borrowRecordId;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(LocalDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
