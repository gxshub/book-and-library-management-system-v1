package com.csci318.libraryservice;

import com.csci318.libraryservice.domain.BookInventory;
import com.csci318.libraryservice.domain.Library;
import com.csci318.libraryservice.repository.BookInventoryRepository;
import com.csci318.libraryservice.repository.LibraryRepository;
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
    private BookInventoryRepository inventoryRepository;

    @BeforeEach
    public void setupData() {
        inventoryRepository.deleteAll();
        libraryRepository.deleteAll();

        libraryRepository.save(new Library("LIB-001", "Central Library", "123 Main St"));
        inventoryRepository.save(new BookInventory("LIB-001", "978-0134685991", 5, 3));
    }

    @Test
    public void findLibrariesByIsbn_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/libraries/by-isbn/978-0134685991"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", instanceOf(java.util.List.class)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].libraryId", is("LIB-001")));
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
                .andExpect(MockMvcResultMatchers.status().isNotFound());
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
    public void borrowBook_BookNotFoundInMasterCatalog() throws Exception {
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:8081/api/v1/books/978-9999999999"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

        Map<String, String> request = new HashMap<>();
        request.put("customerId", "CUST-1001");

        mockMvc.perform(MockMvcRequestBuilders.post("/libraries/LIB-001/books/978-9999999999/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
