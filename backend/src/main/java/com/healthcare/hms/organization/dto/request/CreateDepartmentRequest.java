package com.healthcare.hms.organization.dto.request;

import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.DepartmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Create department payload. Tenant and hospital are resolved server-side.
 */
public record CreateDepartmentRequest(
        @NotBlank(message = "Department name is required")
        @Size(min = 2, max = 200, message = "Department name must be between 2 and 200 characters")
        String name,

        @NotBlank(message = "Department code is required")
        @Size(min = 2, max = 50, message = "Department code must be between 2 and 50 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$",
                message = "Department code may contain letters, digits, underscore, and hyphen"
        )
        String code,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Department type is required")
        DepartmentType departmentType,

        @NotNull(message = "Department status is required")
        DepartmentStatus status,

        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        UUID headUserId
) {
}
