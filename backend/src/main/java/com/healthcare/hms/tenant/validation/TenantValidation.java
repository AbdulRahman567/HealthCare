package com.healthcare.hms.tenant.validation;

import com.healthcare.hms.tenant.context.TenantContext;
import java.util.UUID;

/**
 * Validates a resolved tenant id before it is bound into {@code TenantContextHolder}.
 *
 * <p>Checks: tenant exists, not soft-deleted, status ACTIVE, and does not conflict
 * with an authenticated principal's tenant id.
 */
public interface TenantValidation {

    /**
     * Validates the tenant and returns a request-scoped context ready to bind.
     */
    TenantContext validate(UUID tenantId);
}
