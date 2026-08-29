package com.csci318.libraryservice.domain.domainevent;

import com.csci318.libraryservice.domain.valueobject.CustomerId;

import java.time.LocalDateTime;

public record LoanRenewed(String loanId, CustomerId customerId, LocalDateTime newDueDate, int renewalCount, LocalDateTime occurredAt) {
}
