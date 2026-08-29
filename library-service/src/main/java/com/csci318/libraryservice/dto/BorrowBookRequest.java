package com.csci318.libraryservice.dto;

import jakarta.validation.constraints.NotBlank;

public record BorrowBookRequest(@NotBlank(message = "Customer ID is required") String customerId) {
}
