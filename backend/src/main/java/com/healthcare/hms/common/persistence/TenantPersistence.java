package com.healthcare.hms.common.persistence;

/**
 * Shared constants for Hibernate tenant isolation filters (Phase 2.4).
 */
public final class TenantPersistence {

    /**
     * Hibernate filter applied to every {@link TenantOwnedEntity} (and selectively to
     * platform-shared types such as {@code Role}).
     */
    public static final String FILTER_NAME = "tenantFilter";

    /**
     * Named filter parameter bound to {@link com.healthcare.hms.tenant.context.TenantContextHolder}.
     */
    public static final String PARAM_TENANT_ID = "tenantId";

    /**
     * Strict isolation: row must belong to the current tenant.
     */
    public static final String CONDITION_STRICT = "tenant_id = :" + PARAM_TENANT_ID;

    /**
     * Isolation that also admits platform system-role catalog rows
     * ({@code tenant_id IS NULL AND system_role = true}).
     */
    public static final String CONDITION_INCLUDE_PLATFORM_SYSTEM =
            "tenant_id = :" + PARAM_TENANT_ID + " OR (tenant_id IS NULL AND system_role = true)";

    private TenantPersistence() {
    }
}
