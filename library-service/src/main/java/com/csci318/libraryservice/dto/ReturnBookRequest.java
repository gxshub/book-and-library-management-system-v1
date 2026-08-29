package com.csci318.libraryservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ReturnBookRequest(@NotBlank(message = "Customer ID is required") String customerId) {
}
