package com.csci318.libraryservice.domain.domainservice;

import com.csci318.libraryservice.domain.aggregateroot.Loan;
import com.csci318.libraryservice.domain.valueobject.LibraryId;
import com.csci318.libraryservice.domain.valueobject.Money;
import com.csci318.libraryservice.domain.valueobject.PolicySnapshot;
import com.csci318.libraryservice.exception.ResourceNotFoundException;
import com.csci318.libraryservice.persistence.entity.LibraryPolicyEntity;
import com.csci318.libraryservice.persistence.repository.LibraryPolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CirculationPolicyService {

    private final LibraryPolicyRepository libraryPolicyRepository;

    public CirculationPolicyService(LibraryPolicyRepository libraryPolicyRepository) {
        this.libraryPolicyRepository = libraryPolicyRepository;
    }

    public LocalDateTime calculateDueDate(LocalDateTime borrowedAt, PolicySnapshot snapshot) {
        return borrowedAt.plusDays(snapshot.getLoanPeriodDays());
    }

    public boolean isRenewalAllowed(Loan loan, LocalDateTime now) {
        Loan.LoanStatus effectiveStatus = loan.statusAt(now);
        return (effectiveStatus == Loan.LoanStatus.BORROWED || effectiveStatus == Loan.LoanStatus.OVERDUE)
                && loan.getLoanPeriod().getRenewalCount() < loan.getPolicySnapshot().getMaxRenewals();
    }

    public PolicySnapshot captureSnapshot(LibraryId libraryId) {
        LibraryPolicyEntity policy = libraryPolicyRepository.findById(libraryId.value())
                .orElseThrow(() -> new ResourceNotFoundException("Library " + libraryId.value() + " was not found"));
        return new PolicySnapshot(
                policy.getLoanPeriodDays(),
                policy.getMaxRenewals(),
                new Money(policy.getDailyOverdueFineAmount(), policy.getCurrency()),
                new Money(policy.getLostCopyChargeAmount(), policy.getCurrency()),
                new Money(policy.getDamagedCopyChargeAmount(), policy.getCurrency()));
    }
}
