package com.healthcare.hms.prescriptions.dto.request;

import com.healthcare.hms.prescriptions.validation.ValidPrescriptionItems;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Create a digital prescription with one or more medicine lines.
 */
@ValidPrescriptionItems
public record CreatePrescriptionRequest(
        @NotNull(message = "Consultation id is required")
        UUID consultationId,

        LocalDate prescriptionDate,

        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        String notes,

        /** When true, prescription is issued immediately after create. */
        Boolean issueImmediately,

        @NotEmpty(message = "At least one prescription item is required")
        @Valid
        List<PrescriptionItemRequest> items
) {
}
