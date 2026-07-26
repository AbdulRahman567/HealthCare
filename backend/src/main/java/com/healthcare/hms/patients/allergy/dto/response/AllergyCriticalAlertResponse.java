package com.healthcare.hms.patients.allergy.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Critical allergy alerts for a patient (life-threatening / flagged).
 */
public record AllergyCriticalAlertResponse(
        UUID patientId,
        long criticalAlertCount,
        List<AllergyResponse> criticalAllergies
) {
}
