package com.healthcare.hms.appointments.queue.enums;

/**
 * Patient position in a doctor's daily OPD queue.
 *
 * <pre>
 * CHECKED_IN → WAITING → IN_CONSULTATION → COMPLETED
 * CHECKED_IN | WAITING → MISSED | CANCELLED
 * IN_CONSULTATION → COMPLETED | CANCELLED
 * </pre>
 *
 * <p>Check-in is the front-desk action that creates the entry at {@link #CHECKED_IN}
 * and assigns an automatic queue number for that doctor/day.
 */
public enum QueueEntryStatus {

    /** Arrived and registered on the daily queue. */
    CHECKED_IN,

    /** Ready to be called; waiting room. */
    WAITING,

    /** Currently with the doctor. */
    IN_CONSULTATION,

    /** Consultation finished. */
    COMPLETED,

    /** Called / waited but patient did not attend. */
    MISSED,

    /** Removed from queue (left / admin cancel). */
    CANCELLED
}
