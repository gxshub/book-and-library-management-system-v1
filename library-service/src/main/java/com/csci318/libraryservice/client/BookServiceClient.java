package com.csci318.libraryservice.client;

import com.csci318.libraryservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class BookServiceClient {

    private final RestClient restClient;
    private final String bookServiceBaseUrl;

    public BookServiceClient(RestClient.Builder restClientBuilder, @Value("${book-service.base-url}") String bookServiceBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.bookServiceBaseUrl = bookServiceBaseUrl;
    }

    public void validateBookExists(String isbn) {
        try {
            restClient.get()
                    .uri(bookServiceBaseUrl + "/books/{isbn}", isbn)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Book with ISBN " + isbn + " not found in master catalog");
        }
    }
}
