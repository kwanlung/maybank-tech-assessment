package com.maybank.maybank_assessment.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ApiError error = handler.handleValidationException(ex);

        assertEquals(400, error.getStatus());
        assertEquals("Bad Request", error.getError());
        assertTrue(error.getMessage().contains("field: must not be null"));
    }

    @Test
    void testHandleNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Not found");
        ApiError error = handler.handleNotFound(ex);

        assertEquals(404, error.getStatus());
        assertEquals("Not Found", error.getError());
        assertEquals("Not found", error.getMessage());
    }

    @Test
    void testHandleConflict_ObjectOptimisticLockingFailureException() {
        ObjectOptimisticLockingFailureException ex = mock(ObjectOptimisticLockingFailureException.class);
        when(ex.getMessage()).thenReturn("Optimistic lock error");
        ApiError error = handler.handleConflict(ex);

        assertEquals(409, error.getStatus());
        assertEquals("Conflict", error.getError());
        assertTrue(error.getMessage().contains("Optimistic lock error"));
    }

    @Test
    void testHandleConflict_DataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Integrity error");
        ApiError error = handler.handleConflict(ex);

        assertEquals(409, error.getStatus());
        assertEquals("Conflict", error.getError());
        assertTrue(error.getMessage().contains("Integrity error"));
    }

    @Test
    void testHandleGeneralError() {
        Exception ex = new Exception("Some error");
        ApiError error = handler.handleGeneralError(ex);

        assertEquals(500, error.getStatus());
        assertEquals("Internal Server Error", error.getError());
        assertEquals("An unexpected error occurred.", error.getMessage());
    }
}
