package com.healthcare.hms.clinical.dto.response;

/**
 * Clinical summary and documentation fields for a consultation encounter.
 */
public record ClinicalSummaryResponse(
        String chiefComplaint,
        String historyOfPresentIllness,
        String physicalExamination,
        String doctorNotes,
        String summary,
        String advice
) {
}
