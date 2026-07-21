package com.healthcare.hms.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Base type for tenant-scoped entities. {@code tenantId} may be null for platform-level records
 * such as Super Admin users or system roles.
 */
@MappedSuperclass
public abstract class TenantAwareEntity extends BaseEntity {

    @Column(name = "tenant_id", columnDefinition = "CHAR(36)")
    private UUID tenantId;

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(final UUID tenantId) {
        this.tenantId = tenantId;
    }
}
