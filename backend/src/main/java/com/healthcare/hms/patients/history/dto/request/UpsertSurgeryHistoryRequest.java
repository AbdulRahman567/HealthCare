package com.healthcare.hms.patients.history.dto.request;

import com.healthcare.hms.patients.history.enums.ClinicalConditionStatus;
import com.healthcare.hms.patients.history.enums.ClinicalSeverity;
import com.healthcare.hms.patients.history.enums.ProcedureCategory;
import com.healthcare.hms.patients.history.validation.ValidClinicalDateRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@ValidClinicalDateRange
public record UpsertSurgeryHistoryRequest(
        @NotBlank(message = "Procedure name is required")
        @Size(max = 200, message = "Procedure name must not exceed 200 characters")
        String procedureName,

        @NotNull(message = "Procedure category is required")
        ProcedureCategory procedureCategory,

        @Size(max = 32, message = "Procedure code must not exceed 32 characters")
        String procedureCode,

        @Size(max = 200, message = "Performing facility must not exceed 200 characters")
        String performingFacility,

        @NotNull(message = "Procedure date is required")
        @PastOrPresent(message = "Procedure date must be in the past or today")
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
