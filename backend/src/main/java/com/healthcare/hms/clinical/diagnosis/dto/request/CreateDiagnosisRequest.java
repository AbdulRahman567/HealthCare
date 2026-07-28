package com.healthcare.hms.clinical.diagnosis.dto.request;

import com.healthcare.hms.clinical.diagnosis.validation.ValidIcd10Code;
import com.healthcare.hms.clinical.enums.DiagnosisSeverity;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import com.healthcare.hms.clinical.enums.DiagnosisType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * Add a structured diagnosis row to a consultation.
 *
 * <p>Supports primary, secondary, and working (differential) diagnoses with optional
 * ICD-10 coding. Sequence number is auto-assigned when omitted.
 */
public record CreateDiagnosisRequest(
        @NotBlank(message = "Diagnosis name is required")
        @Size(max = 200, message = "Diagnosis name must not exceed 200 characters")
        String diagnosisName,

        @ValidIcd10Code
        @Size(max = 32, message = "ICD code must not exceed 32 characters")
        String icdCode,

        @NotNull(message = "Diagnosis type is required")
        DiagnosisType diagnosisType,

        DiagnosisStatus diagnosisStatus,

        DiagnosisSeverity severity,

        @Min(value = 1, message = "Sequence number must be at least 1")
        Integer sequenceNumber,

        @Size(max = 1000, message = "Clinical notes must not exceed 1000 characters")
        String clinicalNotes,

        /** Optional explicit diagnosing doctor; defaults to consultation attending doctor. */
        UUID diagnosingDoctorId,

        /** Optional explicit timestamp; defaults to server time when omitted. */
        Instant diagnosedAt
) {
}
