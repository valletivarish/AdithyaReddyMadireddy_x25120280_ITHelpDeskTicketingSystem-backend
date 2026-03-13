package com.helpdesk.exception;

/**
 * Custom exception thrown when a request contains invalid data or violates business rules.
 * Handled by GlobalExceptionHandler to return a 400 HTTP response.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
