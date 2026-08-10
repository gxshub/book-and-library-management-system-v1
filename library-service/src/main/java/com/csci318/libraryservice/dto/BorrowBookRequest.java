package com.csci318.libraryservice.dto;

import jakarta.validation.constraints.NotBlank;

public class BorrowBookRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    public BorrowBookRequest() {
    }

    public BorrowBookRequest(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
