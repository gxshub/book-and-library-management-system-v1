package com.csci318.libraryservice.persistence.repository;

import com.csci318.libraryservice.domain.aggregateroot.FineLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FineLedgerRepository extends JpaRepository<FineLedger, String> {
    Optional<FineLedger> findByLoanId(String loanId);
}
