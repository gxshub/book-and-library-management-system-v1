package com.csci318.libraryservice.exception;

public class InventoryInvariantViolationException extends RuntimeException {

    public InventoryInvariantViolationException(String message) {
        super(message);
    }
}
