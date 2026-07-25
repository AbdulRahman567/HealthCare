package com.healthcare.hms.organization.dto.response;

import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.enums.EmploymentType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PharmacistResponse(
        UUID id,
        UUID tenantId,
        UUID hospitalId,
        UUID userId,
        UUID departmentId,
        UUID reportsToStaffId,
        String employeeCode,
        String jobTitle,
        EmploymentStatus employmentStatus,
        EmploymentType employmentType,
        LocalDate hiredAt,
        LocalDate terminatedAt,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy,
        UUID updatedBy,
        Long version,
        String licenseNumber,
        String pharmacyLocation,
        String qualification
) {
}
