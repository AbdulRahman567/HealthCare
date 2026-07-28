package com.healthcare.hms.clinical.vitals.validation;

import java.math.BigDecimal;

/**
 * Shared clinical rules for vital-signs validation (healthcare-domain aligned).
 *
 * <p>Ranges reflect survivable human physiology for acute-care documentation — not
 * "normal" reference ranges. Trend analysis uses stored time-series rows.
 */
public final class VitalSignsClinicalRules {

    private VitalSignsClinicalRules() {
    }

    public static boolean hasAnyMeasurement(
            final BigDecimal temperatureCelsius,
            final Integer heartRateBpm,
            final Integer systolicBp,
            final Integer diastolicBp,
            final Integer respiratoryRate,
            final BigDecimal oxygenSaturationPercent,
            final BigDecimal heightCm,
            final BigDecimal weightKg,
            final Integer painScale
    ) {
        return temperatureCelsius != null
                || heartRateBpm != null
                || systolicBp != null
                || diastolicBp != null
                || respiratoryRate != null
                || oxygenSaturationPercent != null
                || heightCm != null
                || weightKg != null
                || painScale != null;
    }

    /**
     * Blood pressure must be documented as a pair — partial BP is clinically ambiguous.
     */
    public static boolean isBloodPressureComplete(final Integer systolicBp, final Integer diastolicBp) {
        if (systolicBp == null && diastolicBp == null) {
            return true;
        }
        return systolicBp != null && diastolicBp != null;
    }

    public static boolean isBloodPressurePairValid(final Integer systolicBp, final Integer diastolicBp) {
        if (systolicBp == null || diastolicBp == null) {
            return true;
        }
        return systolicBp > diastolicBp;
    }
}
