package com.healthcare.hms.prescriptions.dto.response;

import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Full prescription with medicine lines.
 */
public record PrescriptionResponse(
        UUID id,
        String prescriptionNumber,
        UUID consultationId,
        String consultationNumber,
        UUID hospitalId,
        UUID patientId,
        String patientName,
        String patientMrn,
        UUID doctorId,
        String doctorName,
        UUID departmentId,
        String departmentName,
        LocalDate prescriptionDate,
        PrescriptionStatus status,
        String notes,
        Instant issuedAt,
        Instant cancelledAt,
        String cancelReason,
        Instant dispensedAt,
        String pharmacyReference,
        List<PrescriptionItemResponse> items,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
