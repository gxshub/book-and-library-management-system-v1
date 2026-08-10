package com.csci318.libraryservice.repository;

import com.csci318.libraryservice.domain.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepository extends JpaRepository<Library, String> {
}
