package com.showmantra.exceptions;

/**
 * Exception thrown when a requested resource (e.g., User, Movie, Show, Booking) is not found in the database.
 * This will be caught by the GlobalExceptionHandler and returned as a 404 Not Found HTTP response.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
