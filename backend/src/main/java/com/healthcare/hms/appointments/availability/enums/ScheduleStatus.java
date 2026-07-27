package com.healthcare.hms.appointments.availability.enums;

/**
 * Operational status of a recurring doctor schedule template.
 */
public enum ScheduleStatus {

    /** Participates in availability and overlap checks. */
    ACTIVE,

    /** Retained for history; ignored for booking / overlap with other active schedules. */
    INACTIVE
}
