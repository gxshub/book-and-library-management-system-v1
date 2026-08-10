package com.csci318.bookservice.dto;

import jakarta.validation.constraints.NotBlank;

public class BookUpdateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    public BookUpdateRequest() {
    }

    public BookUpdateRequest(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
