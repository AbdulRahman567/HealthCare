package com.healthcare.hms.appointments.enums;

/**
 * Clinical / operational category of the booked appointment slot.
 *
 * <p>Distinct from {@link VisitType}, which describes how the patient presents
 * (new vs follow-up vs walk-in). Type answers <em>what</em> was scheduled.
 */
public enum AppointmentType {

    /** Standard outpatient doctor consultation. */
    CONSULTATION,

    /** Planned follow-up for an existing clinical concern. */
    FOLLOW_UP,

    /** Procedure / minor intervention slot (non-admission). */
    PROCEDURE,

    /** Urgent / emergency booking outside normal elective flow. */
    EMERGENCY,

    /** Remote consultation (video / phone). */
    TELEHEALTH
}
