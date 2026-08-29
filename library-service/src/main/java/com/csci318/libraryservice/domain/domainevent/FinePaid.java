package com.csci318.libraryservice.domain.domainevent;

import com.csci318.libraryservice.domain.valueobject.CustomerId;
import com.csci318.libraryservice.domain.valueobject.Money;

import java.time.LocalDateTime;

public record FinePaid(String fineId, CustomerId customerId, Money amount, LocalDateTime occurredAt) {
}
