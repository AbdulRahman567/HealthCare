package com.healthcare.hms.clinical.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Finalize a consultation with optional closing documentation.
 */
public record CompleteConsultationRequest(
        @Size(max = 2000, message = "Clinical summary must not exceed 2000 characters")
        String summary,

        @Size(max = 2000, message = "Advice must not exceed 2000 characters")
        String advice
) {
}
