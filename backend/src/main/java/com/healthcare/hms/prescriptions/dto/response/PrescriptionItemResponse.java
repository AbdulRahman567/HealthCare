package com.healthcare.hms.prescriptions.dto.response;

import com.healthcare.hms.prescriptions.enums.MedicationRoute;
import java.time.Instant;
import java.util.UUID;

/**
 * Single medicine line on a prescription.
 */
public record PrescriptionItemResponse(
        UUID id,
        UUID prescriptionId,
        String medicineName,
        UUID medicineId,
        String medicineCode,
        String dosage,
        String frequency,
        MedicationRoute route,
        String duration,
        String instructions,
        Integer quantity,
        Integer refills,
        Integer sequenceNumber,
        Boolean beforeFood,
        Boolean afterFood,
        Instant createdAt,
        Long version
) {
}
