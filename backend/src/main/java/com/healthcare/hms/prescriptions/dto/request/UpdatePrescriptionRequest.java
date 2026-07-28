package com.healthcare.hms.prescriptions.dto.request;

import com.healthcare.hms.prescriptions.validation.ValidPrescriptionItems;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Update a DRAFT prescription header and optionally replace all line items.
 */
@ValidPrescriptionItems
public record UpdatePrescriptionRequest(
        LocalDate prescriptionDate,

        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        String notes,

        /**
         * When present, replaces the full item set (must remain non-empty and duplicate-free).
         * When null, existing items are left unchanged.
         */
        @Valid
        List<PrescriptionItemRequest> items
) {
}
