package com.healthcare.hms.patients.allergy.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Patient-chart banner surface for allergies (safety-critical).
 *
 * <p>Always returned when opening a patient chart so allergies cannot be missed.
 */
public record AllergyBannerResponse(
        UUID patientId,
        boolean hasCriticalAlerts,
        long criticalAlertCount,
        boolean hasActiveDrugAllergies,
        boolean noKnownDrugAllergies,
        List<AllergyResponse> bannerAllergies
) {
}
