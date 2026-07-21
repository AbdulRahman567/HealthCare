package com.healthcare.hms.common.exception.auth;

/**
 * Raised when login credentials are invalid or authentication otherwise fails.
 */
public class InvalidCredentialsException extends AuthenticationException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid email or password");
    }

    public InvalidCredentialsException(final String message) {
        super("INVALID_CREDENTIALS", message);
    }
}
