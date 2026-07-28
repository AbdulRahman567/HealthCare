package com.healthcare.hms.clinical.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Update consultation clinical documentation while the encounter is editable.
 */
public record UpdateConsultationDocumentationRequest(
        @Size(max = 2000, message = "Chief complaint must not exceed 2000 characters")
        String chiefComplaint,

        @Size(max = 4000, message = "History of present illness must not exceed 4000 characters")
        String historyOfPresentIllness,

        @Size(max = 4000, message = "Physical examination must not exceed 4000 characters")
        String physicalExamination,

        @Size(max = 4000, message = "Doctor notes must not exceed 4000 characters")
        String doctorNotes,

        @Size(max = 2000, message = "Clinical summary must not exceed 2000 characters")
        String summary,

        @Size(max = 2000, message = "Advice must not exceed 2000 characters")
        String advice
) {
}
