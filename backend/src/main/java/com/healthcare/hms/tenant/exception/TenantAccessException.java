package com.healthcare.hms.tenant.exception;

import com.healthcare.hms.tenant.enums.TenantStatus;

/**
 * Compatibility facade for domain lifecycle callers.
 * Prefer the specific subclasses for new middleware code.
 */
public final class TenantAccessException {

    private TenantAccessException() {
    }

    public static TenantNotActiveException notOperational(final TenantStatus status) {
        return new TenantNotActiveException(status);
    }

    public static TenantInvalidTransitionException invalidTransition(
            final TenantStatus from,
            final TenantStatus to
    ) {
        return new TenantInvalidTransitionException(from, to);
    }
}
