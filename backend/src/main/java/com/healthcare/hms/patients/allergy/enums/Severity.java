package com.healthcare.hms.patients.allergy.enums;

/**
 * Allergy severity — safety-critical vocabulary distinct from general clinical severity.
 *
 * <p>{@link #LIFE_THREATENING} always implies critical + banner alerts.
 */
public enum Severity {

    MILD,
    MODERATE,
    SEVERE,

    /** Anaphylaxis-class risk; must never be missable on the chart. */
    LIFE_THREATENING
}
