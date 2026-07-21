package com.healthcare.hms.common.exception.auth;

/**
 * Raised when token type, claims, or signature context fail validation rules.
 */
public class TokenValidationException extends AuthenticationException {

    public TokenValidationException(final String message) {
        super("AUTH_TOKEN_VALIDATION", message);
    }

    public TokenValidationException(final String message, final Throwable cause) {
        super("AUTH_TOKEN_VALIDATION", message, cause);
    }
}
