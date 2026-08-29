package com.csci318.libraryservice.domain.domainservice;

import com.csci318.libraryservice.domain.aggregateroot.Loan;
import com.csci318.libraryservice.domain.valueobject.Money;
import com.csci318.libraryservice.domain.valueobject.PolicySnapshot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class FinePolicyService {

    public Money calculateOverdueFine(Loan loan, LocalDateTime now) {
        if (!now.isAfter(loan.getLoanPeriod().getDueDate())) {
            return Money.zero(loan.getPolicySnapshot().getDailyOverdueFine().currency());
        }
        long overdueDays = Math.max(1, ChronoUnit.DAYS.between(loan.getLoanPeriod().getDueDate(), now));
        BigDecimal amount = loan.getPolicySnapshot().getDailyOverdueFine().amount().multiply(BigDecimal.valueOf(overdueDays));
        return new Money(amount, loan.getPolicySnapshot().getDailyOverdueFine().currency());
    }

    public Money calculateLostCharge(PolicySnapshot snapshot) {
        return snapshot.getLostCopyCharge();
    }

    public Money calculateDamagedCharge(PolicySnapshot snapshot) {
        return snapshot.getDamagedCopyCharge();
    }
}
