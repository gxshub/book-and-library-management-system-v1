package com.csci318.libraryservice.repository;

import com.csci318.libraryservice.domain.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, String> {
    Optional<BorrowRecord> findByIdAndLibraryIdAndIsbnAndCustomerIdAndStatus(
            String id, String libraryId, String isbn, String customerId, BorrowRecord.BorrowStatus status);
}
