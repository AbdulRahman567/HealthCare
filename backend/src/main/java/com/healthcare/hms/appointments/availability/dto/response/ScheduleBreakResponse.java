package com.healthcare.hms.appointments.availability.dto.response;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleBreakResponse(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String label,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
