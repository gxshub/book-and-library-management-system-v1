package com.csci318.libraryservice.domain.aggregateroot;

import com.csci318.libraryservice.domain.domainevent.BookBorrowed;
import com.csci318.libraryservice.domain.domainevent.BookReturned;
import com.csci318.libraryservice.domain.domainevent.CopyReportedDamaged;
import com.csci318.libraryservice.domain.domainevent.CopyReportedLost;
import com.csci318.libraryservice.domain.domainevent.LoanOverdueFlagged;
import com.csci318.libraryservice.domain.domainevent.LoanRenewed;
import com.csci318.libraryservice.domain.valueobject.CustomerId;
import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LoanPeriod;
import com.csci318.libraryservice.domain.valueobject.LibraryId;
import com.csci318.libraryservice.domain.valueobject.PolicySnapshot;
import com.csci318.libraryservice.exception.LoanPolicyViolationException;
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
@Table(name = "loans")
public class Loan {

    public enum LoanStatus {
        BORROWED,
        RETURNED,
        OVERDUE,
        LOST,
        DAMAGED
    }

    @Id
    private String loanId;

    @Embedded
    @AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "library_id"))
    private LibraryId libraryId;

    @Embedded
    @AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "isbn"))
    private Isbn isbn;

    @Embedded
    @AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "customer_id"))
    private CustomerId customerId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "borrowedAt", column = @jakarta.persistence.Column(name = "borrowed_at")),
            @AttributeOverride(name = "dueDate", column = @jakarta.persistence.Column(name = "due_date")),
            @AttributeOverride(name = "renewalCount", column = @jakarta.persistence.Column(name = "renewal_count"))
    })
    private LoanPeriod loanPeriod;

    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "loanPeriodDays", column = @jakarta.persistence.Column(name = "loan_period_days")),
            @AttributeOverride(name = "maxRenewals", column = @jakarta.persistence.Column(name = "max_renewals")),
            @AttributeOverride(name = "dailyOverdueFine.amount", column = @jakarta.persistence.Column(name = "policy_daily_overdue_fine_amount")),
            @AttributeOverride(name = "dailyOverdueFine.currency", column = @jakarta.persistence.Column(name = "policy_daily_overdue_fine_currency")),
            @AttributeOverride(name = "lostCopyCharge.amount", column = @jakarta.persistence.Column(name = "policy_lost_copy_charge_amount")),
            @AttributeOverride(name = "lostCopyCharge.currency", column = @jakarta.persistence.Column(name = "policy_lost_copy_charge_currency")),
            @AttributeOverride(name = "damagedCopyCharge.amount", column = @jakarta.persistence.Column(name = "policy_damaged_copy_charge_amount")),
            @AttributeOverride(name = "damagedCopyCharge.currency", column = @jakarta.persistence.Column(name = "policy_damaged_copy_charge_currency"))
    })
    private PolicySnapshot policySnapshot;

    protected Loan() {
    }

    public Loan(String loanId, LibraryId libraryId, Isbn isbn, CustomerId customerId, LoanPeriod loanPeriod,
                LocalDateTime returnedAt, LoanStatus status, PolicySnapshot policySnapshot) {
        this.loanId = loanId;
        this.libraryId = libraryId;
        this.isbn = isbn;
        this.customerId = customerId;
        this.loanPeriod = loanPeriod;
        this.returnedAt = returnedAt;
        this.status = status;
        this.policySnapshot = policySnapshot;
    }

    public Loan(String loanId, String libraryId, String isbn, String customerId, LocalDateTime borrowedAt,
                LocalDateTime dueDate, LocalDateTime returnedAt, int renewalCount, LoanStatus status,
                int loanPeriodDays, int maxRenewals, BigDecimal dailyOverdueFineAmount,
                BigDecimal lostCopyChargeAmount, BigDecimal damagedCopyChargeAmount, String currency) {
        this(loanId, LibraryId.of(libraryId), Isbn.of(isbn), CustomerId.of(customerId),
                new LoanPeriod(borrowedAt, dueDate, renewalCount), returnedAt, status,
                new PolicySnapshot(loanPeriodDays, maxRenewals,
                        new com.csci318.libraryservice.domain.valueobject.Money(dailyOverdueFineAmount, currency),
                        new com.csci318.libraryservice.domain.valueobject.Money(lostCopyChargeAmount, currency),
                        new com.csci318.libraryservice.domain.valueobject.Money(damagedCopyChargeAmount, currency)));
    }

    public static Loan borrow(String loanId, LibraryId libraryId, Isbn isbn, CustomerId customerId,
                              PolicySnapshot policySnapshot, LocalDateTime borrowedAt, LocalDateTime dueDate) {
        return new Loan(loanId, libraryId, isbn, customerId,
                new LoanPeriod(borrowedAt, dueDate, 0), null, LoanStatus.BORROWED, policySnapshot);
    }

    public BookBorrowed borrowEvent(LocalDateTime occurredAt) {
        return new BookBorrowed(loanId, libraryId, isbn, customerId, occurredAt);
    }

    public BookReturned markReturned(LocalDateTime occurredAt) {
        ensureActiveForMutation("Only active loans can be returned");
        this.returnedAt = occurredAt;
        this.status = LoanStatus.RETURNED;
        return new BookReturned(loanId, libraryId, isbn, customerId, occurredAt);
    }

    public LoanRenewed renew(LocalDateTime occurredAt, LocalDateTime newDueDate) {
        ensureRenewable();
        loanPeriod.renewTo(newDueDate);
        status = LoanStatus.BORROWED;
        return new LoanRenewed(loanId, customerId, newDueDate, loanPeriod.getRenewalCount(), occurredAt);
    }

    public LoanOverdueFlagged markOverdue(LocalDateTime occurredAt) {
        if (status == LoanStatus.BORROWED && occurredAt.isAfter(loanPeriod.getDueDate())) {
            status = LoanStatus.OVERDUE;
            return new LoanOverdueFlagged(loanId, customerId, libraryId, isbn, loanPeriod.getDueDate(), occurredAt);
        }
        return null;
    }

    public CopyReportedLost reportLost(LocalDateTime occurredAt) {
        ensureActiveForMutation("Only active loans can be reported as lost or damaged");
        status = LoanStatus.LOST;
        return new CopyReportedLost(loanId, libraryId, isbn, customerId, occurredAt);
    }

    public CopyReportedDamaged reportDamaged(LocalDateTime occurredAt) {
        ensureActiveForMutation("Only active loans can be reported as lost or damaged");
        status = LoanStatus.DAMAGED;
        return new CopyReportedDamaged(loanId, libraryId, isbn, customerId, occurredAt);
    }

    public LoanStatus statusAt(LocalDateTime now) {
        if (status == LoanStatus.BORROWED && now.isAfter(loanPeriod.getDueDate())) {
            return LoanStatus.OVERDUE;
        }
        return status;
    }

    private void ensureRenewable() {
        if ((status != LoanStatus.BORROWED && status != LoanStatus.OVERDUE)
                || loanPeriod.getRenewalCount() >= policySnapshot.getMaxRenewals()) {
            throw new LoanPolicyViolationException("Loan cannot be renewed under the current policy");
        }
    }

    private void ensureActiveForMutation(String message) {
        if (status != LoanStatus.BORROWED && status != LoanStatus.OVERDUE) {
            throw new LoanPolicyViolationException(message);
        }
    }

    public String getLoanId() {
        return loanId;
    }

    public LibraryId getLibraryId() {
        return libraryId;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public LoanPeriod getLoanPeriod() {
        return loanPeriod;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public PolicySnapshot getPolicySnapshot() {
        return policySnapshot;
    }
}
