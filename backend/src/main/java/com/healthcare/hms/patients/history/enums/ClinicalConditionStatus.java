package com.healthcare.hms.patients.history.enums;

/**
 * Longitudinal status of a clinical history entry.
 *
 * <pre>
 * ONGOING    — still active / unresolved
 * CONTROLLED — chronic/managed but not cured
 * RECOVERED  — resolved; recoveryDate should be set
 * </pre>
 */
public enum ClinicalConditionStatus {

    ONGOING,
    CONTROLLED,
    RECOVERED
}
