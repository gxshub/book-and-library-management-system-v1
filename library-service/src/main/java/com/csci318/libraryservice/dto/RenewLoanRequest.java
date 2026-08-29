package com.csci318.libraryservice.dto;

public record RenewLoanRequest(RequestedBy requestedBy) {

    public enum RequestedBy {
        CUSTOMER,
        LIBRARIAN
    }
}
