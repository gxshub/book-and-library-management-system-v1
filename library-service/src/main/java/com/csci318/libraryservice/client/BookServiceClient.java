package com.csci318.libraryservice.client;

import com.csci318.libraryservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class BookServiceClient {

    private final RestClient.Builder restClientBuilder;
    private final String bookServiceBaseUrl;

    public BookServiceClient(RestClient.Builder restClientBuilder, @Value("${book-service.base-url}") String bookServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.bookServiceBaseUrl = bookServiceBaseUrl;
    }

    public void validateBookExists(String isbn) {
        try {
            restClientBuilder.build().get()
                    .uri(bookServiceBaseUrl + "/books/{isbn}", isbn)
                    .retrieve()
                    .onStatus(status -> status.equals(HttpStatus.NOT_FOUND), (req, resp) -> {
                        throw new ResourceNotFoundException("Book with ISBN " + isbn + " not found in master catalog");
                    })
                    .toEntity(Map.class);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate book existence with Book Service: " + e.getMessage(), e);
        }
    }
}
