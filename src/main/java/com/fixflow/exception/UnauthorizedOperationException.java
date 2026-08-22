package com.fixflow.exception;

/**
 * Thrown when a user attempts an action for which they lack authorization or ownership.
 */
public class UnauthorizedOperationException extends RuntimeException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }

    public UnauthorizedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
