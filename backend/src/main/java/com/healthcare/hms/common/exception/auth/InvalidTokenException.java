package com.healthcare.hms.common.exception.auth;

/**
 * Raised when a JWT is malformed, unsigned incorrectly, or otherwise invalid.
 */
public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException(final String message) {
        super("AUTH_INVALID_TOKEN", message);
    }

    public InvalidTokenException(final String message, final Throwable cause) {
        super("AUTH_INVALID_TOKEN", message, cause);
    }
}
