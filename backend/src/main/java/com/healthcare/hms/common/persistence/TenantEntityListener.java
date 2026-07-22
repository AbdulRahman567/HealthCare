package com.healthcare.hms.common.persistence;

import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.exception.TenantMismatchException;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.Optional;
import java.util.UUID;

/**
 * Stamps and guards {@code tenant_id} on {@link TenantAwareEntity} subclasses.
 *
 * <ul>
 *   <li>{@code @PrePersist} — if unset, copies the request tenant from
 *       {@link TenantContextHolder}; if set, must match the request tenant when present.</li>
 *   <li>{@code @PreUpdate} — rejects cross-tenant mutation and rejects tenant-scoped
 *       sessions mutating platform rows ({@code tenant_id IS NULL}).</li>
 * </ul>
 *
 * <p>When no tenant context is bound (login, JWT validation, Super Admin, hospital
 * registration), an explicitly assigned {@code tenantId} is left unchanged so bootstrap
 * flows keep working.
 */
public class TenantEntityListener {

    @PrePersist
    public void onPrePersist(final TenantAwareEntity entity) {
        final Optional<UUID> contextTenantId = TenantContextHolder.getTenantId();
        final UUID entityTenantId = entity.getTenantId();

        if (entityTenantId == null) {
            if (contextTenantId.isPresent()) {
                // Tenant-scoped requests must not create platform-owned rows by omission.
                entity.setTenantId(contextTenantId.get());
            }
            return;
        }

        if (contextTenantId.isPresent() && !contextTenantId.get().equals(entityTenantId)) {
            throw TenantMismatchException.persistenceTenantMismatch();
        }
    }

    @PreUpdate
    public void onPreUpdate(final TenantAwareEntity entity) {
        final Optional<UUID> contextTenantId = TenantContextHolder.getTenantId();
        if (contextTenantId.isEmpty()) {
            return;
        }

        final UUID entityTenantId = entity.getTenantId();
        if (entityTenantId == null) {
            throw TenantMismatchException.persistencePlatformRowMutation();
        }
        if (!contextTenantId.get().equals(entityTenantId)) {
            throw TenantMismatchException.persistenceTenantMismatch();
        }
    }
}
