package com.csci318.libraryservice.persistence.repository;

import com.csci318.libraryservice.persistence.entity.InventoryEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryEventRepository extends JpaRepository<InventoryEventEntity, Long> {
}
