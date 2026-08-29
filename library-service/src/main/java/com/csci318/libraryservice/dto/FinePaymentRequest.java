package com.csci318.libraryservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FinePaymentRequest(
        @NotNull(message = "Amount is required") @Valid MoneyDto amount,
        @NotBlank(message = "Payer is required") String paidBy) {
}
