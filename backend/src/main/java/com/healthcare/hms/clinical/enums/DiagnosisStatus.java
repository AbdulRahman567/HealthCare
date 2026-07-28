package com.healthcare.hms.clinical.enums;

/**
 * Status of a diagnosis recorded during a consultation.
 *
 * <p>Distinct from patient longitudinal history {@code ClinicalConditionStatus}
 * — this tracks the encounter-specific diagnostic assertion.
 */
public enum DiagnosisStatus {

    PROVISIONAL,
    CONFIRMED,
    RULED_OUT,
    RESOLVED
}
