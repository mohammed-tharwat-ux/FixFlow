package com.fixflow.exception;

/**
 * Thrown when user authentication fails (e.g. bad credentials, inactive account).
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
