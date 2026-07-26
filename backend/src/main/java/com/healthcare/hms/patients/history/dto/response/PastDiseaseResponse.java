package com.healthcare.hms.patients.history.dto.response;

import com.healthcare.hms.patients.history.enums.ClinicalConditionStatus;
import com.healthcare.hms.patients.history.enums.ClinicalSeverity;
import com.healthcare.hms.patients.history.enums.DiseaseCategory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PastDiseaseResponse(
        UUID id,
        UUID patientId,
        UUID medicalHistoryId,
        String diseaseName,
        DiseaseCategory diseaseCategory,
        String diseaseCode,
        LocalDate diagnosisDate,
        LocalDate recoveryDate,
        ClinicalSeverity severity,
        ClinicalConditionStatus conditionStatus,
        String clinicalNotes,
        UUID recordedByUserId,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
