package com.healthcare.hms.common.storage;

/**
 * Thrown when object storage operations fail.
 */
public class ObjectStorageException extends RuntimeException {

    public ObjectStorageException(final String message) {
        super(message);
    }

    public ObjectStorageException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
