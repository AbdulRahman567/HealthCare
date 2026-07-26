package com.healthcare.hms.patients.enums;

/**
 * Biological / administrative sex recorded on the patient chart.
 *
 * <p>Clinical downstream modules (labs, dosing heuristics) may interpret
 * {@link #MALE} / {@link #FEMALE} specifically; {@link #OTHER} and
 * {@link #UNKNOWN} must not be coerced into either without clinician input.
 */
public enum Gender {

    MALE,

    FEMALE,

    /** Recorded sex that is not male or female (e.g. intersex / self-described). */
    OTHER,

    /** Not yet collected or deliberately withheld. */
    UNKNOWN
}
