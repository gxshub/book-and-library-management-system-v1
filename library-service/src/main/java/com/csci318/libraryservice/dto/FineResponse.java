package com.csci318.libraryservice.dto;

public record FineResponse(
        String fineId,
        String loanId,
        String customerId,
        MoneyDto assessed,
        MoneyDto paid,
        MoneyDto balance,
        String status) {
}
