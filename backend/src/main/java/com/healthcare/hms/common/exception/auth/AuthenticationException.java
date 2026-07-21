package com.healthcare.hms.common.exception.auth;

import com.healthcare.hms.common.exception.ApplicationException;

/**
 * Base exception for authentication and token processing failures.
 */
public abstract class AuthenticationException extends ApplicationException {

    protected AuthenticationException(final String errorCode, final String message) {
        super(errorCode, message);
    }

    protected AuthenticationException(final String errorCode, final String message, final Throwable cause) {
        super(errorCode, message, cause);
    }
}
