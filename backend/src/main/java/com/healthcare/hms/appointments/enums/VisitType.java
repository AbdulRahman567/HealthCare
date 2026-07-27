package com.healthcare.hms.appointments.enums;

/**
 * How the patient presents for the appointment.
 *
 * <p>Distinct from {@link AppointmentType} (what was booked). Visit type answers
 * <em>how</em> the encounter is classified for registration and billing workflows.
 */
public enum VisitType {

    /** First visit for this concern / new registration encounter. */
    NEW,

    /** Return visit related to prior care. */
    FOLLOW_UP,

    /** Arrived without a prior booked slot (may still create an appointment row). */
    WALK_IN,

    /** Remote presentation (aligns with telehealth booking). */
    TELECONSULTATION
}
