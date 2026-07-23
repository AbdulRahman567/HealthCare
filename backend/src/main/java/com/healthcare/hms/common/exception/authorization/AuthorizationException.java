package com.healthcare.hms.common.exception.authorization;

import com.healthcare.hms.common.exception.ApplicationException;

/**
 * Base type for authorization (authZ) failures — distinct from authentication (authN).
 *
 * <p>Mapped to HTTP 403. Never include secrets, tokens, or PHI in messages.
 */
public abstract class AuthorizationException extends ApplicationException {

    protected AuthorizationException(final String errorCode, final String message) {
        super(errorCode, message);
    }

    protected AuthorizationException(final String errorCode, final String message, final Throwable cause) {
        super(errorCode, message, cause);
    }
}
