package com.csci318.libraryservice.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class LoanPeriod {

    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private int renewalCount;

    protected LoanPeriod() {
    }

    public LoanPeriod(LocalDateTime borrowedAt, LocalDateTime dueDate, int renewalCount) {
        if (borrowedAt == null || dueDate == null) {
            throw new IllegalArgumentException("Loan period timestamps are required");
        }
        if (renewalCount < 0) {
            throw new IllegalArgumentException("Renewal count must be non-negative");
        }
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.renewalCount = renewalCount;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public void renewTo(LocalDateTime dueDate) {
        this.dueDate = dueDate;
        this.renewalCount = this.renewalCount + 1;
    }
}
