package com.healthcare.hms.common.exception;

/**
 * Base runtime exception for application-level domain and infrastructure errors.
 */
public abstract class ApplicationException extends RuntimeException {

    private final String errorCode;

    protected ApplicationException(final String errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected ApplicationException(final String errorCode, final String message, final Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
