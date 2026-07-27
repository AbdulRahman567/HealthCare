package com.healthcare.hms.appointments.availability.enums;

/**
 * Recurrence pattern for a doctor availability schedule.
 *
 * <p>{@link #WEEKLY} is the Phase 6.2 baseline. Additional patterns are reserved
 * for future recurring-schedule expansion without schema redesign.
 */
public enum ScheduleRecurrenceType {

    /** Repeats every calendar week on the configured working days/hours. */
    WEEKLY,

    /** Reserved — every two weeks (not enforced in Phase 6.2 booking yet). */
    BIWEEKLY,

    /** Reserved — custom interval rules. */
    CUSTOM
}
