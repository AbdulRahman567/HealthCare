package com.healthcare.hms.clinical.diagnosis.dto.request;

import com.healthcare.hms.clinical.diagnosis.validation.ValidIcd10Code;
import com.healthcare.hms.clinical.enums.DiagnosisSeverity;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import com.healthcare.hms.clinical.enums.DiagnosisType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * Correct or update a diagnosis while the parent consultation remains editable.
 */
public record UpdateDiagnosisRequest(
        @Size(max = 200, message = "Diagnosis name must not exceed 200 characters")
        String diagnosisName,

        @ValidIcd10Code
        @Size(max = 32, message = "ICD code must not exceed 32 characters")
        String icdCode,

        DiagnosisType diagnosisType,

        DiagnosisStatus diagnosisStatus,

        DiagnosisSeverity severity,

        @Min(value = 1, message = "Sequence number must be at least 1")
        Integer sequenceNumber,

        @Size(max = 1000, message = "Clinical notes must not exceed 1000 characters")
        String clinicalNotes,

        UUID diagnosingDoctorId,

        Instant diagnosedAt
) {
}
