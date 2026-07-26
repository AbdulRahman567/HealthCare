package com.healthcare.hms.patients.immunization.enums;

/**
 * Lifecycle of an immunization chart entry.
 *
 * <pre>
 * ADMINISTERED      — dose given (default)
 * SCHEDULED         — planned / booked dose
 * REFUSED           — patient/guardian refused
 * ENTERED_IN_ERROR  — mistaken entry (soft-kept for audit)
 * </pre>
 */
public enum ImmunizationStatus {

    ADMINISTERED,
    SCHEDULED,
    REFUSED,
    ENTERED_IN_ERROR
}
