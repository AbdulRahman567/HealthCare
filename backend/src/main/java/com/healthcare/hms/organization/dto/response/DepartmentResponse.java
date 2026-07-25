package com.healthcare.hms.organization.dto.response;

import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.DepartmentType;
import com.healthcare.hms.organization.enums.StaffType;
import java.time.Instant;
import java.util.UUID;

/**
 * Department API response.
 */
public record DepartmentResponse(
        UUID id,
        UUID tenantId,
        UUID hospitalId,
        String name,
        String code,
        String description,
        DepartmentType departmentType,
        DepartmentStatus status,
        String location,
        UUID headUserId,
        UUID headStaffId,
        StaffType headStaffType,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy,
        UUID updatedBy,
        Long version
) {
}
