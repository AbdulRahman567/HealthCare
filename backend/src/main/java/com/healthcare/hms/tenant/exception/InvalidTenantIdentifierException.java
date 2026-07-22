package com.healthcare.hms.tenant.exception;

/**
 * Raised when {@code X-Tenant-ID} (or another identifier) is present but malformed.
 */
public class InvalidTenantIdentifierException extends TenantException {

    public InvalidTenantIdentifierException(final String message) {
        super("INVALID_TENANT_IDENTIFIER", message);
    }
}
