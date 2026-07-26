package com.healthcare.hms.patients.timeline.enums;

/**
 * Clinical timeline event categories.
 *
 * <p>Implemented sources emit events today. {@link #VISIT}, {@link #PRESCRIPTION},
 * {@link #LAB_RESULT}, and {@link #BILLING} are reserved for later modules — they
 * appear in the API contract so clients can filter ahead of time, but produce no
 * rows until a {@code TimelineEventProvider} registers for them.
 */
public enum TimelineEventType {

    /** Patient registration (MRN assigned). */
    REGISTRATION,

    /** Past disease history entry. */
    PAST_DISEASE,

    /** Surgical history entry. */
    SURGERY,

    /** Chronic condition first documented. */
    CHRONIC_CONDITION,

    /** Allergy assertion recorded. */
    ALLERGY,

    /** Vaccination / immunization dose. */
    IMMUNIZATION,

    /** Encounter visit (Phase 7+). */
    VISIT,

    /** Prescription lifecycle event (Phase 9+). */
    PRESCRIPTION,

    /** Laboratory result (later phase). */
    LAB_RESULT,

    /** Billing / invoice event (administrative; filter-gated). */
    BILLING
}
