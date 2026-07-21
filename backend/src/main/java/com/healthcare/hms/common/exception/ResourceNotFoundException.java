package com.healthcare.hms.common.exception;

/**
 * Raised when a requested resource cannot be located.
 */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(final String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
