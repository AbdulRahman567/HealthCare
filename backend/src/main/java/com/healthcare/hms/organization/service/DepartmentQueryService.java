package com.healthcare.hms.organization.service;

import com.healthcare.hms.organization.entity.Department;
import java.util.UUID;

/**
 * Read-side department lookups for staff and other modules.
 */
public interface DepartmentQueryService {

    Department requireById(UUID departmentId);

    void assertBelongsToTenant(UUID departmentId, UUID tenantId);
}
