package com.healthcare.hms.appointments.calendar.enums;

/**
 * Calendar presentation grain for appointment scheduling views.
 */
public enum CalendarViewType {

    /** Single calendar day with paginated events. */
    DAILY,

    /** ISO week (Monday–Sunday) containing the anchor date; events paginated. */
    WEEKLY,

    /** Calendar month with per-day status aggregates (no event payload). */
    MONTHLY
}
