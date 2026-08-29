package com.csci318.libraryservice.persistence.repository;

import com.csci318.libraryservice.persistence.entity.LibraryPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryPolicyRepository extends JpaRepository<LibraryPolicyEntity, String> {
}
