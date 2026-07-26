package com.healthcare.hms.patients.immunization.dto.response;

import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import com.healthcare.hms.patients.immunization.enums.VaccineRoute;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ImmunizationResponse(
        UUID id,
        UUID patientId,
        String vaccineName,
        String vaccineCode,
        Integer doseNumber,
        String manufacturer,
        String batchNumber,
        LocalDate administrationDate,
        LocalDate nextDueDate,
        String healthcareProvider,
        VaccineRoute route,
        ImmunizationStatus status,
        String clinicalNotes,
        boolean due,
        UUID recordedByUserId,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
