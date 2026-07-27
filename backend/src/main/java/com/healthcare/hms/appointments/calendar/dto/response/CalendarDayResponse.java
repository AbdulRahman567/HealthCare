package com.healthcare.hms.appointments.calendar.dto.response;

import com.healthcare.hms.appointments.enums.AppointmentStatus;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * One day in a daily/weekly calendar response.
 */
public record CalendarDayResponse(
        LocalDate date,
        long totalCount,
        Map<AppointmentStatus, Long> countsByStatus,
        List<CalendarEventResponse> events
) {
    public static CalendarDayResponse empty(final LocalDate date) {
        return new CalendarDayResponse(date, 0L, new EnumMap<>(AppointmentStatus.class), List.of());
    }
}
