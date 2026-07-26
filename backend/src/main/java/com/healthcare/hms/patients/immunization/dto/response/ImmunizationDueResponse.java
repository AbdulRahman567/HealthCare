package com.healthcare.hms.patients.immunization.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Immunizations with a next-due date on or before today (follow-up surface).
 */
public record ImmunizationDueResponse(
        UUID patientId,
        long dueCount,
        List<ImmunizationResponse> dueImmunizations
) {
}
