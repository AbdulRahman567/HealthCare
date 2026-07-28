package com.healthcare.hms.prescriptions.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Cancel an issued or draft prescription.
 */
public record CancelPrescriptionRequest(
        @Size(max = 500, message = "Cancel reason must not exceed 500 characters")
        String reason
) {
}
