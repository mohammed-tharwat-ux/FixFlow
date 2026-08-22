package com.fixflow.exception;

/**
 * Thrown when a requested ticket cannot be found in the repository.
 */
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String message) {
        super(message);
    }

    public TicketNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
