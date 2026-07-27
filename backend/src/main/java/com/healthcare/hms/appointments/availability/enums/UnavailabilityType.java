package com.healthcare.hms.appointments.availability.enums;

/**
 * Doctor time blocked outside normal recurring schedule.
 */
public enum UnavailabilityType {

    /** Planned leave / vacation / PTO. */
    LEAVE,

    /** Doctor-observed holiday (or hospital holiday applied to the doctor). */
    HOLIDAY,

    /** Ad-hoc emergency block (illness, urgent call-out, etc.). */
    EMERGENCY
}
