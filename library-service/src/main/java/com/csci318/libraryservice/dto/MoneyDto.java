package com.csci318.libraryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record MoneyDto(
        @NotNull(message = "Amount is required") @PositiveOrZero(message = "Amount must be non-negative") BigDecimal amount,
        @NotBlank(message = "Currency is required") String currency) {
}
