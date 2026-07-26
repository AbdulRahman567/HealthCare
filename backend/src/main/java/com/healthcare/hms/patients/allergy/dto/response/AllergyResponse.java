package com.healthcare.hms.patients.allergy.dto.response;

import com.healthcare.hms.patients.allergy.enums.AllergyStatus;
import com.healthcare.hms.patients.allergy.enums.AllergyType;
import com.healthcare.hms.patients.allergy.enums.Reaction;
import com.healthcare.hms.patients.allergy.enums.Severity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AllergyResponse(
        UUID id,
        UUID patientId,
        String allergenName,
        String allergenCode,
        AllergyType allergyType,
        Severity severity,
        Reaction reaction,
        AllergyStatus status,
        LocalDate onsetDate,
        String clinicalNotes,
        boolean verified,
        boolean patientReported,
        boolean criticalAlert,
        boolean showOnBanner,
        boolean lifeThreatening,
        UUID recordedByUserId,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
