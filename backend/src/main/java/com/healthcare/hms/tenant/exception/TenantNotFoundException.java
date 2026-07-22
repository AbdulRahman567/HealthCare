package com.healthcare.hms.tenant.exception;

import java.util.UUID;

/**
 * Raised when a tenant UUID cannot be resolved in the shared schema.
 */
public class TenantNotFoundException extends TenantException {

    public TenantNotFoundException(final UUID tenantId) {
        // UUID is intentionally omitted from the client message (enumeration hardening).
        super("TENANT_NOT_FOUND", requireTenantId(tenantId));
    }

    private static String requireTenantId(final UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        return "Tenant not found";
    }

    public TenantNotFoundException(final String message) {
        super("TENANT_NOT_FOUND", message);
    }
}
