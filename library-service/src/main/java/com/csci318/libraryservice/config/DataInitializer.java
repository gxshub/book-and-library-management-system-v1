package com.csci318.libraryservice.config;

import com.csci318.libraryservice.domain.entity.Customer;
import com.csci318.libraryservice.domain.entity.Library;
import com.csci318.libraryservice.persistence.entity.LibraryPolicyEntity;
import com.csci318.libraryservice.persistence.repository.CustomerRepository;
import com.csci318.libraryservice.persistence.repository.LibraryPolicyRepository;
import com.csci318.libraryservice.persistence.repository.LibraryRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    ApplicationRunner seedReferenceData(
            LibraryRepository libraryRepository,
            CustomerRepository customerRepository,
            LibraryPolicyRepository libraryPolicyRepository) {
        return args -> {
            if (libraryRepository.count() == 0) {
                libraryRepository.save(new Library("LIB-001", "Central Community Library", "123 Main St"));
                libraryRepository.save(new Library("LIB-002", "West End Library", "456 Oak Ave"));
            }

            if (customerRepository.count() == 0) {
                customerRepository.save(new Customer("CUST-1001", "Alice Reader", "alice@example.com", "0400000001"));
                customerRepository.save(new Customer("CUST-1002", "Bob Borrower", "bob@example.com", "0400000002"));
                customerRepository.save(new Customer("CUST-1003", "Cara Customer", "cara@example.com", "0400000003"));
            }

            if (libraryPolicyRepository.count() == 0) {
                libraryPolicyRepository.save(new LibraryPolicyEntity("LIB-001", 14, 2,
                        new BigDecimal("1.50"), new BigDecimal("35.00"), new BigDecimal("20.00"), "AUD"));
                libraryPolicyRepository.save(new LibraryPolicyEntity("LIB-002", 14, 2,
                        new BigDecimal("1.50"), new BigDecimal("35.00"), new BigDecimal("20.00"), "AUD"));
            }
        };
    }
}
