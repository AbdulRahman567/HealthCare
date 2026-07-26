package com.healthcare.hms.patients.history.dto.request;

import com.healthcare.hms.patients.history.enums.ClinicalConditionStatus;
import com.healthcare.hms.patients.history.enums.ClinicalSeverity;
import com.healthcare.hms.patients.history.enums.DiseaseCategory;
import com.healthcare.hms.patients.history.validation.ValidClinicalDateRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@ValidClinicalDateRange
public record UpsertChronicConditionRequest(
        @NotBlank(message = "Condition name is required")
        @Size(max = 200, message = "Condition name must not exceed 200 characters")
        String conditionName,

        @NotNull(message = "Disease category is required")
        DiseaseCategory diseaseCategory,

        @Size(max = 32, message = "Condition code must not exceed 32 characters")
        String conditionCode,

        @NotNull(message = "Diagnosis date is required")
        @PastOrPresent(message = "Diagnosis date must be in the past or today")
        LocalDate diagnosisDate,

        @PastOrPresent(message = "Recovery date must be in the past or today")
        LocalDate recoveryDate,

        @NotNull(message = "Severity is required")
        ClinicalSeverity severity,

        @NotNull(message = "Condition status is required")
        ClinicalConditionStatus conditionStatus,

        @Size(max = 1000, message = "Clinical notes must not exceed 1000 characters")
        String clinicalNotes
) {
}
