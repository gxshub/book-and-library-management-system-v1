package com.csci318.libraryservice.persistence.repository;

import com.csci318.libraryservice.persistence.entity.InventoryReadModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryReadModelRepository extends JpaRepository<InventoryReadModelEntity, String> {
    List<InventoryReadModelEntity> findByIsbnAndAvailableCopiesGreaterThan(String isbn, int availableCopies);
    Optional<InventoryReadModelEntity> findByLibraryIdAndIsbn(String libraryId, String isbn);
}
