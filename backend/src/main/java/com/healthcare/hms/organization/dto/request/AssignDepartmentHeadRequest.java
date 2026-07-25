package com.healthcare.hms.organization.dto.request;

import com.healthcare.hms.organization.enums.StaffType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Designate a staff member as head of a department.
 */
public record AssignDepartmentHeadRequest(
        @NotNull(message = "Staff type is required")
        StaffType staffType,

        @NotNull(message = "Staff id is required")
        UUID staffId
) {
}
