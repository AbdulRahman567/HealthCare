package com.healthcare.hms.appointments.calendar.dto.response;

import com.healthcare.hms.appointments.calendar.enums.CalendarScope;
import com.healthcare.hms.appointments.calendar.enums.CalendarViewType;
import com.healthcare.hms.common.api.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Daily or weekly calendar payload.
 *
 * <p>{@code events} is the paginated flat list (avoids loading unbounded hospital-wide sets).
 * {@code days} groups the <em>current page</em> of events by date for UI rendering.
 */
public record CalendarRangeResponse(
        CalendarScope scope,
        UUID scopeId,
        CalendarViewType view,
        LocalDate fromDate,
        LocalDate toDate,
        List<CalendarDayResponse> days,
        PageResponse<CalendarEventResponse> events
) {
}
