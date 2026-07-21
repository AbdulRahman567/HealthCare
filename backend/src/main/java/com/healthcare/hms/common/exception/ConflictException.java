package com.healthcare.hms.common.exception;

/**
 * Raised when a request conflicts with existing state (e.g. duplicate email).
 */
public class ConflictException extends ApplicationException {

    public ConflictException(final String errorCode, final String message) {
        super(errorCode, message);
    }

    public ConflictException(final String message) {
        super("CONFLICT", message);
    }
}
