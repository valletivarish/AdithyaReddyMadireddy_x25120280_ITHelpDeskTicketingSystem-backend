package com.helpdesk.exception;

/**
 * Custom exception thrown when a requested resource is not found in the database.
 * Handled by GlobalExceptionHandler to return a 404 HTTP response.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
