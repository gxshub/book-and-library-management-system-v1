package com.csci318.libraryservice.dto;

import java.time.LocalDateTime;

public record LoanResponse(
        String loanId,
        String libraryId,
        String isbn,
        String customerId,
        LocalDateTime borrowedAt,
        LocalDateTime dueDate,
        LocalDateTime returnedAt,
        int renewalCount,
        String status) {
}
