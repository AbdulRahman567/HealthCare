package com.healthcare.hms.appointments.calendar.dto.response;

import com.healthcare.hms.appointments.enums.AppointmentStatus;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/**
 * Monthly cell summary — counts only (no event rows; use daily view for detail).
 */
public record CalendarDaySummaryResponse(
        LocalDate date,
        long totalCount,
        Map<AppointmentStatus, Long> countsByStatus
) {
    public static CalendarDaySummaryResponse empty(final LocalDate date) {
        return new CalendarDaySummaryResponse(date, 0L, new EnumMap<>(AppointmentStatus.class));
    }
}
