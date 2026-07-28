package com.healthcare.hms.prescriptions.dto.request;

import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Search / filter criteria for prescription listing.
 */
public record PrescriptionSearchCriteria(
        String prescriptionNumber,
        UUID patientId,
        UUID doctorId,
        UUID consultationId,
        PrescriptionStatus status,
        LocalDate fromDate,
        LocalDate toDate
) {
}
