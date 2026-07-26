package com.healthcare.hms.patients.dto.request;

import com.healthcare.hms.patients.enums.BloodGroup;
import com.healthcare.hms.patients.enums.Gender;
import com.healthcare.hms.patients.enums.PatientStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Patient directory search / filter criteria (Phase 5.7).
 *
 * <p>All filters are applied in the database via JPA Specifications —
 * never in-memory after a full load.
 */
public record PatientSearchCriteria(
        /** Free-text across MRN, name, phone, email, national id (CNIC). */
        String q,
        String mrn,
        String firstName,
        String lastName,
        String phone,
        String email,
        /** National identity / CNIC. */
        String nationalId,
        PatientStatus status,
        BloodGroup bloodGroup,
        Gender gender,
        LocalDate dateOfBirth,
        LocalDate dateOfBirthFrom,
        LocalDate dateOfBirthTo,
        Integer ageMin,
        Integer ageMax,
        UUID departmentId,
        /** Future-ready primary doctor filter (nullable column until assignment workflows). */
        UUID doctorId
) {
}
