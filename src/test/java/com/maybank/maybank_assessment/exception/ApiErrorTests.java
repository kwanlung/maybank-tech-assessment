package com.maybank.maybank_assessment.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTests {

    @Test
    void testAllArgsConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        ApiError error = new ApiError(400, "Bad Request", "Invalid input", now);

        assertEquals(400, error.getStatus());
        assertEquals("Bad Request", error.getError());
        assertEquals("Invalid input", error.getMessage());
        assertEquals(now, error.getTimestamp());
    }

    @Test
    void testBuilder() {
        ApiError error = ApiError.builder()
                .status(404)
                .error("Not Found")
                .message("Resource missing")
                .timestamp(LocalDateTime.now())
                .build();

        assertEquals(404, error.getStatus());
        assertEquals("Not Found", error.getError());
        assertEquals("Resource missing", error.getMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void testThreeArgsConstructorSetsTimestamp() {
        ApiError error = new ApiError(500, "Internal", "Oops");
        assertEquals(500, error.getStatus());
        assertEquals("Internal", error.getError());
        assertEquals("Oops", error.getMessage());
        assertNotNull(error.getTimestamp());
    }
}
