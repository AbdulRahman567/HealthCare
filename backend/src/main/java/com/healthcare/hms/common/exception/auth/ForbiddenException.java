package com.healthcare.hms.common.exception.auth;

/**
 * Raised when an authenticated caller lacks permission for a protected operation.
 */
public class ForbiddenException extends AuthenticationException {

    public ForbiddenException() {
        super("AUTH_FORBIDDEN", "You do not have permission to access this resource");
    }

    public ForbiddenException(final String message) {
        super("AUTH_FORBIDDEN", message);
    }
}
