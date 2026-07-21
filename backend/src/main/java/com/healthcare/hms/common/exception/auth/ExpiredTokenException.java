package com.healthcare.hms.common.exception.auth;

/**
 * Raised when a JWT has passed its configured expiration time.
 */
public class ExpiredTokenException extends AuthenticationException {

    public ExpiredTokenException(final String message) {
        super("AUTH_EXPIRED_TOKEN", message);
    }

    public ExpiredTokenException(final String message, final Throwable cause) {
        super("AUTH_EXPIRED_TOKEN", message, cause);
    }
}
