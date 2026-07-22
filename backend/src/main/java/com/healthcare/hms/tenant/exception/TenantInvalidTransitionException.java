package com.healthcare.hms.tenant.exception;

import com.healthcare.hms.tenant.enums.TenantStatus;

/**
 * Raised when a tenant lifecycle transition is not allowed.
 */
public class TenantInvalidTransitionException extends TenantException {

    public TenantInvalidTransitionException(final TenantStatus from, final TenantStatus to) {
        super(
                "TENANT_INVALID_TRANSITION",
                "Cannot transition tenant from " + from + " to " + to
        );
    }
}
