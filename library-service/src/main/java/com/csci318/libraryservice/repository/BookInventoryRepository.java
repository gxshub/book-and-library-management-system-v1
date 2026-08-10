package com.csci318.libraryservice.repository;

import com.csci318.libraryservice.domain.BookInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {
    List<BookInventory> findByIsbnAndAvailableCopiesGreaterThan(String isbn, int availableCopies);
    Optional<BookInventory> findByLibraryIdAndIsbn(String libraryId, String isbn);
}
