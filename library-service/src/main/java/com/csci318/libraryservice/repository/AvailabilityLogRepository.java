package com.csci318.libraryservice.repository;

import com.csci318.libraryservice.domain.AvailabilityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityLogRepository extends JpaRepository<AvailabilityLog, Long> {
    List<AvailabilityLog> findByLibraryIdAndIsbnOrderByTimestampDesc(String libraryId, String isbn);
}
