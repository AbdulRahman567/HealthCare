package com.healthcare.hms.tenant.context;

import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import java.util.UUID;

/**
 * Immutable, request-scoped tenant identity bound after successful resolution and validation.
 */
public record TenantContext(
        UUID tenantId,
        String slug,
        TenantType tenantType,
        TenantStatus status
) {

    public boolean isOperational() {
        return status == TenantStatus.ACTIVE;
    }
}
