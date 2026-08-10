package com.csci318.bookservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createBook_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("isbn", "978-0134685991");
        request.put("title", "Effective Java");
        request.put("author", "Joshua Bloch");

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn", is("978-0134685991")))
                .andExpect(jsonPath("$.title", is("Effective Java")))
                .andExpect(jsonPath("$.author", is("Joshua Bloch")));
    }

    @Test
    public void createBook_BadRequest_MissingTitle() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("isbn", "978-0000000001");
        request.put("author", "Unknown Author");

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getBookByIsbn_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("isbn", "978-0201633610");
        request.put("title", "Design Patterns");
        request.put("author", "Erich Gamma");

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/books/978-0201633610"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is("978-0201633610")))
                .andExpect(jsonPath("$.title", is("Design Patterns")));
    }

    @Test
    public void getBookByIsbn_NotFound() throws Exception {
        mockMvc.perform(get("/books/NON_EXISTENT_ISBN"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void listAllBooks_Success() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(instanceOf(java.util.List.class))));
    }

    @Test
    public void updateBook_Success() throws Exception {
        Map<String, String> createReq = new HashMap<>();
        createReq.put("isbn", "978-0132350884");
        createReq.put("title", "Clean Code");
        createReq.put("author", "Robert C. Martin");

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        Map<String, String> updateReq = new HashMap<>();
        updateReq.put("title", "Clean Code (2nd Edition)");
        updateReq.put("author", "Robert C. Martin");

        mockMvc.perform(put("/books/978-0132350884")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Clean Code (2nd Edition)")));
    }

    @Test
    public void updateBook_NotFound() throws Exception {
        Map<String, String> updateReq = new HashMap<>();
        updateReq.put("title", "Non Existent Book");
        updateReq.put("author", "No Author");

        mockMvc.perform(put("/books/999-9999999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteBook_Success() throws Exception {
        Map<String, String> createReq = new HashMap<>();
        createReq.put("isbn", "978-0596007126");
        createReq.put("title", "Head First Design Patterns");
        createReq.put("author", "Eric Freeman");

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/books/978-0596007126"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/books/978-0596007126"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteBook_NotFound() throws Exception {
        mockMvc.perform(delete("/books/UNKNOWN_ISBN"))
                .andExpect(status().isNotFound());
    }
}
