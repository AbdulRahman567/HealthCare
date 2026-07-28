package com.healthcare.hms.clinical.vitals.dto.response;

/**
 * Structured blood pressure reading (systolic / diastolic mmHg).
 */
public record BloodPressureResponse(
        Integer systolicMmHg,
        Integer diastolicMmHg
) {
}
