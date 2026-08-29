package com.csci318.libraryservice.exception;

public class FineStateViolationException extends RuntimeException {

    public FineStateViolationException(String message) {
        super(message);
    }
}
