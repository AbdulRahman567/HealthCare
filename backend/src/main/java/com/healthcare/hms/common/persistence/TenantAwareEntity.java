package com.healthcare.hms.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Base type for every row that may carry a hospital tenant under the
 * Shared Database + Shared Schema + Tenant ID isolation strategy.
 *
 * <p>{@code tenantId} references {@code tenants.id}. It may be null only for
 * platform-scoped records (Super Admin users, global system roles).
 *
 * <p>Strict query isolation for tenant-owned business data is implemented by
 * {@link TenantOwnedEntity}. Types that must also see platform rows (for example
 * system {@code Role}s) stay on this class and declare a selective Hibernate filter.
 *
 * <p>Writes are guarded by {@link TenantEntityListener}; reads are constrained by the
 * Hibernate {@link TenantPersistence#FILTER_NAME} when a request tenant is bound.
 */
@MappedSuperclass
@EntityListeners(TenantEntityListener.class)
public abstract class TenantAwareEntity extends BaseEntity {

    @Column(name = "tenant_id", updatable = false, columnDefinition = "CHAR(36)")
    private UUID tenantId;

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(final UUID tenantId) {
        this.tenantId = tenantId;
    }
}
