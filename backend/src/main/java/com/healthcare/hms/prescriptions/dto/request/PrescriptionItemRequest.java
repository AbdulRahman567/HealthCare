package com.healthcare.hms.prescriptions.dto.request;

import com.healthcare.hms.prescriptions.enums.MedicationRoute;
import com.healthcare.hms.prescriptions.validation.ValidPrescriptionItemFoodTiming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Single medicine line on a create/update prescription request.
 */
@ValidPrescriptionItemFoodTiming
public record PrescriptionItemRequest(
        @NotBlank(message = "Medicine name is required")
        @Size(max = 200, message = "Medicine name must not exceed 200 characters")
        String medicineName,

        /** Optional future medicine_master catalog id. */
        UUID medicineId,

        @Size(max = 64, message = "Medicine code must not exceed 64 characters")
        String medicineCode,

        @NotBlank(message = "Dosage is required")
        @Size(max = 100, message = "Dosage must not exceed 100 characters")
        String dosage,

        @NotBlank(message = "Frequency is required")
        @Size(max = 100, message = "Frequency must not exceed 100 characters")
        String frequency,

        @NotNull(message = "Route is required")
        MedicationRoute route,

        @NotBlank(message = "Duration is required")
        @Size(max = 100, message = "Duration must not exceed 100 characters")
        String duration,

        @Size(max = 1000, message = "Instructions must not exceed 1000 characters")
        String instructions,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Min(value = 0, message = "Refills must not be negative")
        Integer refills,

        @Min(value = 1, message = "Sequence number must be at least 1")
        Integer sequenceNumber,

        Boolean beforeFood,

        Boolean afterFood
) {
}
