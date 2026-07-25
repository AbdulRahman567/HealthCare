package com.healthcare.hms.organization.dto.request;

import com.healthcare.hms.organization.enums.StaffType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Transfer a staff member from their current department to another.
 */
public record TransferStaffRequest(
        @NotNull(message = "Staff type is required")
        StaffType staffType,

        @NotNull(message = "Staff id is required")
        UUID staffId,

        @NotNull(message = "Target department id is required")
        UUID toDepartmentId,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
