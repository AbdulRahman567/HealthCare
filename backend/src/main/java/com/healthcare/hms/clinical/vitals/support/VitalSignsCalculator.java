package com.healthcare.hms.clinical.vitals.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes derived vital-sign metrics (BMI from height/weight).
 */
public final class VitalSignsCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int BMI_SCALE = 1;

    private VitalSignsCalculator() {
    }

    /**
     * BMI = weight(kg) / height(m)². Returns {@code null} when height or weight is missing.
     */
    public static BigDecimal computeBmi(final BigDecimal heightCm, final BigDecimal weightKg) {
        if (heightCm == null || weightKg == null || heightCm.signum() <= 0) {
            return null;
        }
        final BigDecimal heightMeters = heightCm.divide(HUNDRED, 4, RoundingMode.HALF_UP);
        final BigDecimal heightSquared = heightMeters.multiply(heightMeters);
        return weightKg.divide(heightSquared, BMI_SCALE, RoundingMode.HALF_UP);
    }
}
