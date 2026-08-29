package com.csci318.libraryservice.persistence.repository;

import com.csci318.libraryservice.domain.aggregateroot.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
}
