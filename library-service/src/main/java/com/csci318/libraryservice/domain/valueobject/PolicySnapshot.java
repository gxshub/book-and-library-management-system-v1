package com.csci318.libraryservice.domain.valueobject;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;

@Embeddable
public class PolicySnapshot {

    private int loanPeriodDays;
    private int maxRenewals;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @jakarta.persistence.Column(name = "policy_daily_overdue_fine_amount")),
            @AttributeOverride(name = "currency", column = @jakarta.persistence.Column(name = "policy_daily_overdue_fine_currency"))
    })
    private Money dailyOverdueFine;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @jakarta.persistence.Column(name = "policy_lost_copy_charge_amount")),
            @AttributeOverride(name = "currency", column = @jakarta.persistence.Column(name = "policy_lost_copy_charge_currency"))
    })
    private Money lostCopyCharge;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @jakarta.persistence.Column(name = "policy_damaged_copy_charge_amount")),
            @AttributeOverride(name = "currency", column = @jakarta.persistence.Column(name = "policy_damaged_copy_charge_currency"))
    })
    private Money damagedCopyCharge;

    protected PolicySnapshot() {
    }

    public PolicySnapshot(int loanPeriodDays, int maxRenewals, Money dailyOverdueFine, Money lostCopyCharge, Money damagedCopyCharge) {
        this.loanPeriodDays = loanPeriodDays;
        this.maxRenewals = maxRenewals;
        this.dailyOverdueFine = dailyOverdueFine;
        this.lostCopyCharge = lostCopyCharge;
        this.damagedCopyCharge = damagedCopyCharge;
    }

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }

    public int getMaxRenewals() {
        return maxRenewals;
    }

    public Money getDailyOverdueFine() {
        return dailyOverdueFine;
    }

    public Money getLostCopyCharge() {
        return lostCopyCharge;
    }

    public Money getDamagedCopyCharge() {
        return damagedCopyCharge;
    }
}
