package com.csci318.libraryservice.exception;

public class LoanPolicyViolationException extends RuntimeException {

    public LoanPolicyViolationException(String message) {
        super(message);
    }
}
