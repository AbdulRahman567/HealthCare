/**
 * Multi-tenant foundation module (Phases 2.1–2.4).
 *
 * <p>Owns the {@code Tenant} aggregate, isolation contracts
 * ({@link com.healthcare.hms.common.persistence.TenantAwareEntity},
 * {@link com.healthcare.hms.common.persistence.TenantOwnedEntity},
 * {@link com.healthcare.hms.tenant.context.TenantContextHolder}), identification /
 * resolution strategies, {@link com.healthcare.hms.tenant.web.TenantFilter} middleware,
 * {@link com.healthcare.hms.tenant.validation.TenantValidation}, Hibernate tenant-filter
 * enablement ({@link com.healthcare.hms.tenant.persistence.TenantPersistenceConfig}),
 * and the tenant exception hierarchy.
 *
 * <p>Hospital registration, department management, and clinical modules consume this
 * foundation; they must not redefine tenant isolation rules.
 */
package com.healthcare.hms.tenant;
