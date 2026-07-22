package com.healthcare.hms.tenant.exception;

import com.healthcare.hms.tenant.enums.TenantStatus;

/**
 * Raised when a tenant exists but is not ACTIVE / operational.
 */
public class TenantNotActiveException extends TenantException {

    public TenantNotActiveException(final TenantStatus status) {
        super(
                "TENANT_NOT_OPERATIONAL",
                "Tenant is not operational (status=" + status + ")"
        );
    }

    public TenantNotActiveException(final String message) {
        super("TENANT_NOT_OPERATIONAL", message);
    }
}
