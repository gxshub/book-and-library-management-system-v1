package com.csci318.libraryservice.dto;

import jakarta.validation.constraints.NotBlank;

public class ReturnBookRequest {

    @NotBlank(message = "Borrow Record ID is required")
    private String borrowRecordId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    public ReturnBookRequest() {
    }

    public ReturnBookRequest(String borrowRecordId, String customerId) {
        this.borrowRecordId = borrowRecordId;
        this.customerId = customerId;
    }

    public String getBorrowRecordId() {
        return borrowRecordId;
    }

    public void setBorrowRecordId(String borrowRecordId) {
        this.borrowRecordId = borrowRecordId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
