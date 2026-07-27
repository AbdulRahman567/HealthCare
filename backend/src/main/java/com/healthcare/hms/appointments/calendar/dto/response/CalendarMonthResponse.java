package com.healthcare.hms.appointments.calendar.dto.response;

import com.healthcare.hms.appointments.calendar.enums.CalendarScope;
import java.util.List;
import java.util.UUID;

/**
 * Monthly calendar overview — one aggregation query, no per-day N+1.
 */
public record CalendarMonthResponse(
        CalendarScope scope,
        UUID scopeId,
        int year,
        int month,
        long totalAppointments,
        List<CalendarDaySummaryResponse> days
) {
}
