package com.healthcare.hms.common.persistence;

import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;

/**
 * Base type for entities that always belong to exactly one tenant.
 *
 * <p>When {@link com.healthcare.hms.tenant.context.TenantContextHolder} is bound,
 * Hibernate automatically appends {@code tenant_id = :tenantId} to every entity query
 * and collection fetch against subclasses — including Spring Data {@code findById},
 * derived queries, and JPQL. Repositories therefore cannot accidentally return another
 * tenant's rows without explicitly disabling the filter.
 *
 * <p>Prefer this over per-repository {@code Specification}s or duplicated
 * {@code findByTenantId...} methods for isolation.
 */
@MappedSuperclass
@Filter(name = TenantPersistence.FILTER_NAME, condition = TenantPersistence.CONDITION_STRICT)
public abstract class TenantOwnedEntity extends TenantAwareEntity {
}
