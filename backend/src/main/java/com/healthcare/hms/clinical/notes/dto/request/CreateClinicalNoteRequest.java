package com.healthcare.hms.clinical.notes.dto.request;

import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * Create a structured clinical note on a consultation.
 */
public record CreateClinicalNoteRequest(
        @NotNull(message = "Note type is required")
        ClinicalNoteType noteType,

        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Note content is required")
        @Size(max = 4000, message = "Note content must not exceed 4000 characters")
        String content,

        /** Optional author doctor; defaults to consultation attending doctor. */
        UUID authorDoctorId,

        /** Optional timestamp; defaults to server time. */
        Instant recordedAt
) {
}
