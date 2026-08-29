package com.csci318.libraryservice.domain.aggregateroot;

import com.csci318.libraryservice.domain.domainevent.FinePaid;
import com.csci318.libraryservice.domain.valueobject.CustomerId;
import com.csci318.libraryservice.domain.valueobject.Money;
import com.csci318.libraryservice.exception.FineStateViolationException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fine_ledgers")
public class FineLedger {

    public enum FineStatus {
        OPEN,
        SETTLED
    }

    @Id
    private String fineId;
    private String loanId;

    @Embedded
    @AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "customer_id"))
    private CustomerId customerId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @jakarta.persistence.Column(name = "assessed_amount")),
            @AttributeOverride(name = "currency", column = @jakarta.persistence.Column(name = "assessed_currency"))
    })
    private Money assessed;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @jakarta.persistence.Column(name = "paid_amount")),
            @AttributeOverride(name = "currency", column = @jakarta.persistence.Column(name = "paid_currency"))
    })
    private Money paid;

    @Enumerated(EnumType.STRING)
    private FineStatus status;

    protected FineLedger() {
    }

    public FineLedger(String fineId, String loanId, CustomerId customerId, Money assessed, Money paid, FineStatus status) {
        this.fineId = fineId;
        this.loanId = loanId;
        this.customerId = customerId;
        this.assessed = assessed;
        this.paid = paid;
        this.status = status;
    }

    public FineLedger(String fineId, String loanId, String customerId, BigDecimal assessedAmount, BigDecimal paidAmount, String currency, FineStatus status) {
        this(fineId, loanId, CustomerId.of(customerId), new Money(assessedAmount, currency), new Money(paidAmount, currency), status);
    }

    public void assess(Money amount, String reason, LocalDateTime occurredAt) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Fine assessment reason is required");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Fine assessment timestamp is required");
        }
        assessed = assessed.add(amount);
        status = balance().amount().compareTo(BigDecimal.ZERO) == 0 ? FineStatus.SETTLED : FineStatus.OPEN;
    }

    public FinePaid recordPayment(Money amount, LocalDateTime occurredAt) {
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new FineStateViolationException("Fine payment amount must be greater than zero");
        }
        if (amount.amount().compareTo(balance().amount()) > 0) {
            throw new FineStateViolationException("Payment exceeds outstanding balance");
        }
        paid = paid.add(amount);
        status = balance().amount().compareTo(BigDecimal.ZERO) == 0 ? FineStatus.SETTLED : FineStatus.OPEN;
        return new FinePaid(fineId, customerId, amount, occurredAt);
    }

    public Money balance() {
        return assessed.subtract(paid);
    }

    public String getFineId() {
        return fineId;
    }

    public String getLoanId() {
        return loanId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getAssessed() {
        return assessed;
    }

    public Money getPaid() {
        return paid;
    }

    public FineStatus getStatus() {
        return status;
    }
}
