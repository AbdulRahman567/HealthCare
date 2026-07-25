package com.healthcare.hms.organization.dto.response;

import com.healthcare.hms.organization.enums.AssignmentAction;
import com.healthcare.hms.organization.enums.StaffType;
import java.time.Instant;
import java.util.UUID;

/**
 * Staff ↔ department assignment history entry.
 */
public record StaffAssignmentResponse(
        UUID id,
        UUID tenantId,
        UUID hospitalId,
        StaffType staffType,
        UUID staffId,
        UUID departmentId,
        UUID fromDepartmentId,
        AssignmentAction action,
        String reason,
        Instant assignedAt,
        Instant endedAt,
        UUID assignedBy,
        UUID endedBy,
        boolean open,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
