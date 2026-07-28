package com.healthcare.hms.clinical.diagnosis.dto.response;

import com.healthcare.hms.clinical.enums.DiagnosisSeverity;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import com.healthcare.hms.clinical.enums.DiagnosisType;
import java.time.Instant;
import java.util.UUID;

/**
 * Structured consultation diagnosis snapshot.
 */
public record DiagnosisResponse(
        UUID id,
        UUID consultationId,
        String consultationNumber,
        UUID patientId,
        UUID diagnosingDoctorId,
        String diagnosingDoctorName,
        String diagnosisName,
        String icdCode,
        DiagnosisType diagnosisType,
        DiagnosisStatus diagnosisStatus,
        DiagnosisSeverity severity,
        Instant diagnosedAt,
        Integer sequenceNumber,
        String clinicalNotes,
        Instant createdAt,
        Long version
) {
}
