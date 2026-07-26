package com.healthcare.hms.patients.history.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full structured medical-history view for a patient.
 */
public record MedicalHistoryResponse(
        UUID id,
        UUID patientId,
        Instant lastReviewedAt,
        UUID lastReviewedBy,
        List<PastDiseaseResponse> pastDiseases,
        List<SurgeryHistoryResponse> surgeries,
        List<ChronicConditionResponse> chronicConditions,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
