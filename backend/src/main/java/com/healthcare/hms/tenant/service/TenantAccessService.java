package com.healthcare.hms.tenant.service;

import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.entity.Tenant;
import java.util.UUID;

/**
 * Application service for tenant lookup and operational validation.
 * Does not expose hospital registration or profile APIs (those belong to later phases).
 */
public interface TenantAccessService {

    /**
     * Loads a tenant by id or fails if missing / soft-deleted.
     */
    Tenant requireTenant(UUID tenantId);

    /**
     * Loads a tenant and asserts {@link Tenant#isOperational()}.
     */
    Tenant requireActiveTenant(UUID tenantId);

    /**
     * Builds a request-scoped context after successful resolution + validation.
     */
    TenantContext toContext(Tenant tenant);
}
