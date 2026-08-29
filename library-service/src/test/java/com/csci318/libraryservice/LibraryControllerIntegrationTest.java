package com.csci318.libraryservice;

import com.csci318.libraryservice.domain.aggregateroot.FineLedger;
import com.csci318.libraryservice.domain.aggregateroot.InventoryItem;
import com.csci318.libraryservice.domain.aggregateroot.Loan;
import com.csci318.libraryservice.domain.entity.Customer;
import com.csci318.libraryservice.domain.entity.Library;
import com.csci318.libraryservice.persistence.entity.InventoryReadModelEntity;
import com.csci318.libraryservice.persistence.entity.LibraryPolicyEntity;
import com.csci318.libraryservice.persistence.repository.CustomerRepository;
import com.csci318.libraryservice.persistence.repository.FineLedgerRepository;
import com.csci318.libraryservice.persistence.repository.InventoryEventRepository;
import com.csci318.libraryservice.persistence.repository.InventoryItemRepository;
import com.csci318.libraryservice.persistence.repository.InventoryReadModelRepository;
import com.csci318.libraryservice.persistence.repository.LibraryPolicyRepository;
import com.csci318.libraryservice.persistence.repository.LibraryRepository;
import com.csci318.libraryservice.persistence.repository.LoanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
public class LibraryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LibraryPolicyRepository libraryPolicyRepository;

    @Autowired
    private InventoryReadModelRepository inventoryReadModelRepository;

    @Autowired
    private InventoryEventRepository inventoryEventRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private FineLedgerRepository fineLedgerRepository;

    @BeforeEach
    public void setupData() {
        fineLedgerRepository.deleteAll();
        loanRepository.deleteAll();
        inventoryEventRepository.deleteAll();
        inventoryReadModelRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        libraryPolicyRepository.deleteAll();
        customerRepository.deleteAll();
        libraryRepository.deleteAll();
        mockServer.reset();

        libraryRepository.save(new Library("LIB-001", "Central Community Library", "123 Main St"));
        libraryRepository.save(new Library("LIB-002", "West End Library", "456 Oak Ave"));
        customerRepository.save(new Customer("CUST-1001", "Alice Reader", "alice@example.com", "0400000001"));
        customerRepository.save(new Customer("CUST-1002", "Bob Borrower", "bob@example.com", "0400000002"));
        libraryPolicyRepository.save(new LibraryPolicyEntity("LIB-001", 14, 2,
                new BigDecimal("1.50"), new BigDecimal("35.00"), new BigDecimal("20.00"), "AUD"));
        libraryPolicyRepository.save(new LibraryPolicyEntity("LIB-002", 14, 2,
                new BigDecimal("1.50"), new BigDecimal("35.00"), new BigDecimal("20.00"), "AUD"));
        inventoryItemRepository.save(seedInventory("LIB-001", "978-0134685991", 5, 3));
        inventoryReadModelRepository.save(new InventoryReadModelEntity("LIB-001:978-0134685991", "LIB-001", "978-0134685991", 5, 3, 1));
    }

    @Test
    public void findLibrariesByIsbn_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/libraries/by-isbn/978-0134685991"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", instanceOf(java.util.List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].libraryId", is("LIB-001")));
    }

    @Test
    public void findLibrariesByIsbn_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/libraries/by-isbn/978-0000000000"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("ResourceNotFound")));
    }

    @Test
    public void checkBookAvailability_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/libraries/LIB-001/books/978-0134685991/availability"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.libraryId", is("LIB-001")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.availableCopies", is(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isAvailable", is(true)));
    }

    @Test
    public void checkBookAvailability_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/libraries/LIB-9999/books/978-0000000000/availability"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("ResourceNotFound")));
    }

    @Test
    public void borrowBook_Success() throws Exception {
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:8081/api/v1/books/978-0134685991"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("{\"isbn\":\"978-0134685991\",\"title\":\"Effective Java\",\"author\":\"Joshua Bloch\"}", MediaType.APPLICATION_JSON));

        Map<String, String> request = new HashMap<>();
        request.put("customerId", "CUST-1001");

        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-0134685991/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.libraryId", is("LIB-001")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isbn", is("978-0134685991")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", is("BORROWED")));
    }

    @Test
    public void borrowBook_BadRequest_WhenCustomerMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-0134685991/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("ValidationFailed")));
    }

    @Test
    public void borrowBook_BookNotFoundInMasterCatalog() throws Exception {
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:8081/api/v1/books/978-9999999999"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

        Map<String, String> request = new HashMap<>();
        request.put("customerId", "CUST-1001");

        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-9999999999/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("ResourceNotFound")));
    }

    @Test
    public void borrowBook_Conflict_WhenNoCopiesAvailable() throws Exception {
        inventoryItemRepository.save(seedInventory("LIB-001", "978-0001112223", 1, 0));
        inventoryReadModelRepository.save(new InventoryReadModelEntity("LIB-001:978-0001112223", "LIB-001", "978-0001112223", 1, 0, 1));
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:8081/api/v1/books/978-0001112223"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("{\"isbn\":\"978-0001112223\",\"title\":\"Unavailable\",\"author\":\"Author\"}", MediaType.APPLICATION_JSON));

        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-0001112223/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"CUST-1001\"}"))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("InventoryInvariantViolation")));
    }

    @Test
    public void returnBook_Success() throws Exception {
        loanRepository.save(new Loan("REC-1001", "LIB-001", "978-0134685991", "CUST-1001",
                LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(12), null, 0,
                Loan.LoanStatus.BORROWED, 14, 2, new BigDecimal("1.50"), new BigDecimal("35.00"),
                new BigDecimal("20.00"), "AUD"));
        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-0134685991/loans/REC-1001/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"CUST-1001\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", is("RETURNED")));
    }

    @Test
    public void returnBook_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-0134685991/loans/REC-404/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"CUST-1001\"}"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("ResourceNotFound")));
    }

    @Test
    public void renewLoan_Success() throws Exception {
        loanRepository.save(new Loan("REC-2001", "LIB-001", "978-0134685991", "CUST-1001",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), null, 0,
                Loan.LoanStatus.BORROWED, 14, 2, new BigDecimal("1.50"), new BigDecimal("35.00"),
                new BigDecimal("20.00"), "AUD"));

        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-0134685991/loans/REC-2001/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requestedBy\":\"CUSTOMER\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.renewalCount", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", is("BORROWED")));
    }

    @Test
    public void renewLoan_Conflict_WhenLimitExceeded() throws Exception {
        loanRepository.save(new Loan("REC-2002", "LIB-001", "978-0134685991", "CUST-1001",
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1), null, 2,
                Loan.LoanStatus.BORROWED, 14, 2, new BigDecimal("1.50"), new BigDecimal("35.00"),
                new BigDecimal("20.00"), "AUD"));

        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-0134685991/loans/REC-2002/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requestedBy\":\"CUSTOMER\"}"))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("LoanPolicyViolation")));
    }

    @Test
    public void listActiveLoansForCustomer_Success() throws Exception {
        loanRepository.save(new Loan("REC-3001", "LIB-001", "978-0134685991", "CUST-1001",
                LocalDateTime.now().minusDays(4), LocalDateTime.now().plusDays(10), null, 0,
                Loan.LoanStatus.BORROWED, 14, 2, new BigDecimal("1.50"), new BigDecimal("35.00"),
                new BigDecimal("20.00"), "AUD"));
        loanRepository.save(new Loan("REC-3002", "LIB-001", "978-0134685991", "CUST-1001",
                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(2), null, 1,
                Loan.LoanStatus.BORROWED, 14, 2, new BigDecimal("1.50"), new BigDecimal("35.00"),
                new BigDecimal("20.00"), "AUD"));
        loanRepository.save(new Loan("REC-3003", "LIB-001", "978-0134685991", "CUST-1001",
                LocalDateTime.now().minusDays(12), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(3), 0,
                Loan.LoanStatus.RETURNED, 14, 2, new BigDecimal("1.50"), new BigDecimal("35.00"),
                new BigDecimal("20.00"), "AUD"));

        mockMvc.perform(MockMvcRequestBuilders.get("/customers/CUST-1001/loans").param("status", "ACTIVE"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].status", is("OVERDUE")));
    }

    @Test
    public void listActiveLoansForCustomer_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/customers/CUST-9999/loans"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("ResourceNotFound")));
    }

    @Test
    public void listOverdueLoansByLibrary_Success_WithBucket() throws Exception {
        loanRepository.save(new Loan("REC-4001", "LIB-001", "978-0134685991", "CUST-1001",
                LocalDateTime.now().minusDays(30), LocalDateTime.now().minusDays(8), null, 0,
                Loan.LoanStatus.BORROWED, 14, 2, new BigDecimal("1.50"), new BigDecimal("35.00"),
                new BigDecimal("20.00"), "AUD"));
        loanRepository.save(new Loan("REC-4002", "LIB-001", "978-0134685991", "CUST-1002",
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(3), null, 0,
                Loan.LoanStatus.BORROWED, 14, 2, new BigDecimal("1.50"), new BigDecimal("35.00"),
                new BigDecimal("20.00"), "AUD"));

        mockMvc.perform(MockMvcRequestBuilders.get("/libraries/LIB-001/loans/overdue").param("overdueBucket", "gt7d"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].loanId", is("REC-4001")));
    }

    @Test
    public void listOverdueLoansByLibrary_BadRequest_WhenBucketInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/libraries/LIB-001/loans/overdue").param("overdueBucket", "week1"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("ValidationFailed")));
    }

    @Test
    public void reportLossOrDamage_Success() throws Exception {
        loanRepository.save(new Loan("REC-5001", "LIB-001", "978-0134685991", "CUST-1001",
                LocalDateTime.now().minusDays(4), LocalDateTime.now().plusDays(5), null, 0,
                Loan.LoanStatus.BORROWED, 14, 2, new BigDecimal("1.50"), new BigDecimal("35.00"),
                new BigDecimal("20.00"), "AUD"));

        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-0134685991/loans/REC-5001/report-loss-or-damage")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reportType\":\"DAMAGED\",\"customerId\":\"CUST-1001\",\"notes\":\"Pages missing\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", is("DAMAGED")));
    }

    @Test
    public void adjustInventoryStock_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-0134685991/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"delta\":2,\"reasonCode\":\"ACQUISITION\",\"operatorId\":\"LIBRARIAN-1\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCopies", is(7)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.availableCopies", is(5)));
    }

    @Test
    public void adjustInventoryStock_Conflict_WhenInvariantBroken() throws Exception {
        inventoryItemRepository.save(seedInventory("LIB-001", "978-1111111111", 3, 1));
        inventoryReadModelRepository.save(new InventoryReadModelEntity("LIB-001:978-1111111111", "LIB-001", "978-1111111111", 3, 1, 1));

        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-1111111111/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"delta\":-3,\"reasonCode\":\"DISCARD\",\"operatorId\":\"LIBRARIAN-1\"}"))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("InventoryInvariantViolation")));
    }

    @Test
    public void transferInventory_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isbn\":\"978-0134685991\",\"sourceLibraryId\":\"LIB-001\",\"targetLibraryId\":\"LIB-002\",\"quantity\":2,\"requestedBy\":\"LIBRARIAN-1\"}"))
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", is("ACCEPTED")));
    }

    @Test
    public void transferInventory_Conflict_WhenInsufficientCopies() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/inventory/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isbn\":\"978-0134685991\",\"sourceLibraryId\":\"LIB-001\",\"targetLibraryId\":\"LIB-002\",\"quantity\":4,\"requestedBy\":\"LIBRARIAN-1\"}"))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("InventoryInvariantViolation")));
    }

    @Test
    public void payFine_Success() throws Exception {
        fineLedgerRepository.save(new FineLedger("FINE-8801", "REC-6001", "CUST-1001",
                new BigDecimal("10.50"), BigDecimal.ZERO, "AUD", FineLedger.FineStatus.OPEN));

        mockMvc.perform(MockMvcRequestBuilders.post("/fines/FINE-8801/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":{\"amount\":5.25,\"currency\":\"AUD\"},\"paidBy\":\"CUST-1001\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.paid.amount", is(5.25)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount", is(5.25)));
    }

    @Test
    public void payFine_Conflict_WhenOverpaying() throws Exception {
        fineLedgerRepository.save(new FineLedger("FINE-8802", "REC-6002", "CUST-1001",
                new BigDecimal("10.00"), BigDecimal.ZERO, "AUD", FineLedger.FineStatus.OPEN));

        mockMvc.perform(MockMvcRequestBuilders.post("/fines/FINE-8802/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":{\"amount\":12.00,\"currency\":\"AUD\"},\"paidBy\":\"CUST-1001\"}"))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", is("FineStateViolation")));
    }

    private InventoryItem seedInventory(String libraryId, String isbn, int totalCopies, int availableCopies) {
        InventoryItem item = new InventoryItem(
                com.csci318.libraryservice.domain.valueobject.LibraryId.of(libraryId),
                com.csci318.libraryservice.domain.valueobject.Isbn.of(isbn));
        item.initialize(totalCopies, LocalDateTime.now().minusDays(1));
        int borrowedCopies = totalCopies - availableCopies;
        if (borrowedCopies > 0) {
            for (int i = 0; i < borrowedCopies; i++) {
                item.reserveCopyForLoan("REC-SEED-" + i, LocalDateTime.now().minusHours(1));
            }
        }
        return item;
    }
}
