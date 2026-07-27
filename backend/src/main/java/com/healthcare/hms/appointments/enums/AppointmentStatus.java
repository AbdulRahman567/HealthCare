package com.healthcare.hms.appointments.enums;

/**
 * Appointment lifecycle status (PRD §7.13 + Phase 6.3 confirm).
 *
 * <pre>
 * SCHEDULED  → CONFIRMED   (patient/staff confirmed attendance)
 * SCHEDULED | CONFIRMED → COMPLETED
 * SCHEDULED | CONFIRMED → CANCELLED
 * SCHEDULED | CONFIRMED → MISSED
 * Reschedule keeps the row; confirmation resets to SCHEDULED
 * </pre>
 *
 * <p>Soft delete ({@code deleted = true}) is separate from status and is used
 * for mistaken bookings / compliance removal, not for cancelled appointments.
 */
public enum AppointmentStatus {

    /** Booked and awaiting confirmation or the scheduled time. */
    SCHEDULED,

    /** Attendance confirmed; still occupies the doctor/patient slot. */
    CONFIRMED,

    /** Encounter completed (feeds Phase 7 visit creation later). */
    COMPLETED,

    /** Explicitly cancelled by staff or patient workflow. */
    CANCELLED,

    /** Patient did not attend; slot consumed without completion. */
    MISSED
}
