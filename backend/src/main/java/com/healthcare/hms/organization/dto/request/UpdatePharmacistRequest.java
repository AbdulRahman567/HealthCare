package com.healthcare.hms.organization.dto.request;

import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.enums.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record UpdatePharmacistRequest(

        @NotNull(message = "User id is required")
        UUID userId,

        @NotNull(message = "Department id is required")
        UUID departmentId,

        @NotBlank(message = "Employee code is required")
        @Size(min = 2, max = 50, message = "Employee code must be between 2 and 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$", message = "Employee code may contain letters, digits, underscore, and hyphen")
        String employeeCode,

        @Size(max = 150, message = "Job title must not exceed 150 characters")
        String jobTitle,

        @NotNull(message = "Employment status is required")
        EmploymentStatus employmentStatus,

        @NotNull(message = "Employment type is required")
        EmploymentType employmentType,

        LocalDate hiredAt,

        LocalDate terminatedAt,

        UUID reportsToStaffId,

        @NotBlank(message = "License number is required")
        @Size(max = 100, message = "License number must not exceed 100 characters")
        String licenseNumber,

        @Size(max = 150, message = "Pharmacy location must not exceed 150 characters")
        String pharmacyLocation,

        @Size(max = 255, message = "Qualification must not exceed 255 characters")
        String qualification
) {
}
