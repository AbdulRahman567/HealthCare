package com.healthcare.hms.organization.dto.request;

import com.healthcare.hms.organization.enums.StaffType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Assign a staff member who currently has no department affiliation.
 */
public record AssignStaffRequest(
        @NotNull(message = "Staff type is required")
        StaffType staffType,

        @NotNull(message = "Staff id is required")
        UUID staffId,

        @NotNull(message = "Department id is required")
        UUID departmentId,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
