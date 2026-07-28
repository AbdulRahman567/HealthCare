package com.healthcare.hms.clinical.notes.dto.request;

import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * Update a clinical note while the consultation remains editable.
 */
public record UpdateClinicalNoteRequest(
        ClinicalNoteType noteType,

        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @Size(max = 4000, message = "Note content must not exceed 4000 characters")
        String content,

        UUID authorDoctorId,

        Instant recordedAt
) {
}
