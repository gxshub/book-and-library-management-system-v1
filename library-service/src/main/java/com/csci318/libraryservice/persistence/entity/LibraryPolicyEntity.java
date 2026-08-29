package com.csci318.libraryservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "library_policies")
public class LibraryPolicyEntity {

    @Id
    private String libraryId;
    private int loanPeriodDays;
    private int maxRenewals;
    @Column(precision = 19, scale = 2)
    private BigDecimal dailyOverdueFineAmount;
    @Column(precision = 19, scale = 2)
    private BigDecimal lostCopyChargeAmount;
    @Column(precision = 19, scale = 2)
    private BigDecimal damagedCopyChargeAmount;
    private String currency;

    protected LibraryPolicyEntity() {
    }

    public LibraryPolicyEntity(String libraryId, int loanPeriodDays, int maxRenewals, BigDecimal dailyOverdueFineAmount,
                               BigDecimal lostCopyChargeAmount, BigDecimal damagedCopyChargeAmount, String currency) {
        this.libraryId = libraryId;
        this.loanPeriodDays = loanPeriodDays;
        this.maxRenewals = maxRenewals;
        this.dailyOverdueFineAmount = dailyOverdueFineAmount;
        this.lostCopyChargeAmount = lostCopyChargeAmount;
        this.damagedCopyChargeAmount = damagedCopyChargeAmount;
        this.currency = currency;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }

    public int getMaxRenewals() {
        return maxRenewals;
    }

    public BigDecimal getDailyOverdueFineAmount() {
        return dailyOverdueFineAmount;
    }

    public BigDecimal getLostCopyChargeAmount() {
        return lostCopyChargeAmount;
    }

    public BigDecimal getDamagedCopyChargeAmount() {
        return damagedCopyChargeAmount;
    }

    public String getCurrency() {
        return currency;
    }
}
