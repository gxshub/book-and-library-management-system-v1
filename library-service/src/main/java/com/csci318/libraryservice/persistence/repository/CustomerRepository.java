package com.csci318.libraryservice.persistence.repository;

import com.csci318.libraryservice.domain.entity.Customer;
import com.csci318.libraryservice.domain.valueobject.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {
}
