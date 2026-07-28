package com.healthcare.hms.patients.timeline.enums;

/**
 * Clinical timeline event categories.
 *
 * <p>Implemented sources emit events today. {@link #LAB_RESULT} and {@link #BILLING}
 * are reserved for later modules — they appear in the API contract so clients can filter
 * ahead of time, but produce no rows until a {@code TimelineEventProvider} registers for them.
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

    /** Family / hereditary history entry (Phase 8). */
    FAMILY_HISTORY,

    /** Allergy assertion recorded. */
    ALLERGY,

    /** Vaccination / immunization dose. */
    IMMUNIZATION,

    /** Encounter visit / consultation (Phase 7). */
    VISIT,

    /** Planned follow-up visit (Phase 7.7). */
    FOLLOW_UP,

    /** Prescription lifecycle event (Phase 7.5 / 7.10). */
    PRESCRIPTION,

    /** Laboratory result (later phase). */
    LAB_RESULT,

    /** Billing / invoice event (administrative; filter-gated). */
    BILLING
}
