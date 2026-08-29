package com.csci318.libraryservice.exception;

import org.springframework.http.HttpStatus;
import com.csci318.libraryservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("ResourceNotFound", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(ValidationFailedException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailed(ValidationFailedException ex) {
        return ResponseEntity.badRequest()
                .body(error("ValidationFailed", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage(),
                        (left, right) -> right));
        return ResponseEntity.badRequest()
                .body(error("ValidationFailed", "Validation failed for request", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(error("ValidationFailed", "Request body could not be parsed", Map.of()));
    }

    @ExceptionHandler(InventoryInvariantViolationException.class)
    public ResponseEntity<ErrorResponse> handleInventoryInvariantViolation(InventoryInvariantViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("InventoryInvariantViolation", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(LoanPolicyViolationException.class)
    public ResponseEntity<ErrorResponse> handleLoanPolicyViolation(LoanPolicyViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("LoanPolicyViolation", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(FineStateViolationException.class)
    public ResponseEntity<ErrorResponse> handleFineStateViolation(FineStateViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("FineStateViolation", ex.getMessage(), Map.of()));
    }

    private ErrorResponse error(String code, String message, Map<String, Object> details) {
        return new ErrorResponse(code, message, OffsetDateTime.now(), details);
    }
}
