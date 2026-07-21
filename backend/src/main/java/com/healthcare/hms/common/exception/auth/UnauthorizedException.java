package com.healthcare.hms.common.exception.auth;

/**
 * Raised when authentication is missing or the access token cannot be accepted.
 */
public class UnauthorizedException extends AuthenticationException {

    public UnauthorizedException() {
        super("AUTH_UNAUTHORIZED", "Authentication is required to access this resource");
    }

    public UnauthorizedException(final String message) {
        super("AUTH_UNAUTHORIZED", message);
    }
}
