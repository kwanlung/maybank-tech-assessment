package com.maybank.maybank_assessment.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 400 - Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException ex) {
        // Extract field errors and construct a meaningful message
        StringBuilder errors = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                errors.append(fe.getField()).append(": ").append(fe.getDefaultMessage()).append("; "));
        return new ApiError(400, "Bad Request", errors.toString());
    }

    // 404 - Resource not found
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(EntityNotFoundException ex) {
        return new ApiError(404, "Not Found", ex.getMessage());
    }

    // 409 - Optimistic locking conflict or data integrity violation
    @ExceptionHandler({ ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(Exception ex) {
        return new ApiError(409, "Conflict", "Conflict occurred: " + ex.getMessage());
    }

    // 500 - Internal server errors (fallback)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneralError(Exception ex) {
        // In production, hide internal details
        return new ApiError(500, "Internal Server Error", "An unexpected error occurred.");
    }
}

