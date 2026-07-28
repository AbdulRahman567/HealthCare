package com.healthcare.hms.clinical.dto.response;

import com.healthcare.hms.clinical.enums.ConsultationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ConsultationResponse(
        UUID id,
        String consultationNumber,
        UUID hospitalId,
        UUID patientId,
        String patientName,
        String patientMrn,
        UUID doctorId,
        String doctorName,
        UUID departmentId,
        String departmentName,
        UUID appointmentId,
        LocalDate consultationDate,
        ConsultationStatus status,
        Instant startedAt,
        Instant pausedAt,
        Instant completedAt,
        ClinicalSummaryResponse clinicalSummary,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
