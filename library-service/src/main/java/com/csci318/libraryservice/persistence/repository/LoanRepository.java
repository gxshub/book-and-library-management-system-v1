package com.csci318.libraryservice.persistence.repository;

import com.csci318.libraryservice.domain.aggregateroot.Loan;
import com.csci318.libraryservice.domain.valueobject.CustomerId;
import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LibraryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {
    Optional<Loan> findByLoanIdAndLibraryIdAndIsbn(String loanId, LibraryId libraryId, Isbn isbn);
    Optional<Loan> findByLoanIdAndLibraryIdAndIsbnAndCustomerId(String loanId, LibraryId libraryId, Isbn isbn, CustomerId customerId);
    List<Loan> findByCustomerId(CustomerId customerId);
    List<Loan> findByLibraryId(LibraryId libraryId);
}
