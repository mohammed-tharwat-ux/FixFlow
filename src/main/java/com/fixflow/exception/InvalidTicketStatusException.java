package com.fixflow.exception;

/**
 * Thrown when an illegal ticket status lifecycle transition is attempted.
 */
public class InvalidTicketStatusException extends RuntimeException {

    public InvalidTicketStatusException(String message) {
        super(message);
    }

    public InvalidTicketStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
