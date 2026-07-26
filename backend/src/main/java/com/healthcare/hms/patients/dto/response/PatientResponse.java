package com.healthcare.hms.patients.dto.response;

import com.healthcare.hms.patients.enums.BloodGroup;
import com.healthcare.hms.patients.enums.Gender;
import com.healthcare.hms.patients.enums.MaritalStatus;
import com.healthcare.hms.patients.enums.PatientStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Patient registration / profile API response.
 */
public record PatientResponse(
        UUID id,
        UUID tenantId,
        String mrn,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        BloodGroup bloodGroup,
        String nationalId,
        String phone,
        String email,
        String address,
        EmergencyContactResponse emergencyContact,
        MaritalStatus maritalStatus,
        PatientStatus status,
        UUID primaryDepartmentId,
        UUID primaryDoctorId,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy,
        UUID updatedBy,
        Long version
) {
}
