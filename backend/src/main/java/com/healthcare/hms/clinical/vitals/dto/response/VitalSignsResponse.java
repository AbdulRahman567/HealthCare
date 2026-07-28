package com.healthcare.hms.clinical.vitals.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Single vital-signs measurement snapshot (time-series row).
 */
public record VitalSignsResponse(
        UUID id,
        UUID consultationId,
        String consultationNumber,
        UUID patientId,
        Instant recordedAt,
        UUID recordedByUserId,
        String recordedByName,
        BigDecimal temperatureCelsius,
        Integer heartRateBpm,
        BloodPressureResponse bloodPressure,
        Integer respiratoryRate,
        BigDecimal oxygenSaturationPercent,
        BigDecimal heightCm,
        BigDecimal weightKg,
        BigDecimal bmi,
        Integer painScale,
        String notes,
        Instant createdAt,
        Long version
) {
}
