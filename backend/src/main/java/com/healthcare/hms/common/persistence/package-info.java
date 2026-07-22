/**
 * Shared JPA persistence foundations: auditing, soft delete, and tenant isolation.
 *
 * <p>{@link TenantPersistence#FILTER_NAME} is registered here so all modules share one
 * Hibernate filter definition.
 */
@FilterDef(
        name = TenantPersistence.FILTER_NAME,
        parameters = @ParamDef(
                name = TenantPersistence.PARAM_TENANT_ID,
                type = java.util.UUID.class
        )
)
package com.healthcare.hms.common.persistence;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
