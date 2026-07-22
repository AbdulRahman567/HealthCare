package com.healthcare.hms.tenant.exception;

/**
 * Raised when a tenant-scoped request has no resolvable tenant identity.
 */
public class TenantRequiredException extends TenantException {

    public TenantRequiredException() {
        super(
                "TENANT_REQUIRED",
                "X-Tenant-ID header is required for this request"
        );
    }

    public TenantRequiredException(final String message) {
        super("TENANT_REQUIRED", message);
    }
}
