package com.csci318.libraryservice.persistence.repository;

import com.csci318.libraryservice.domain.entity.Library;
import com.csci318.libraryservice.domain.valueobject.LibraryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepository extends JpaRepository<Library, LibraryId> {
}
