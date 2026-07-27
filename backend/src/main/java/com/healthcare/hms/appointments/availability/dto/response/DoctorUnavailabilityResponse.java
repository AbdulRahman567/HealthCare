package com.healthcare.hms.appointments.availability.dto.response;

import com.healthcare.hms.appointments.availability.enums.UnavailabilityType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record DoctorUnavailabilityResponse(
        UUID id,
        UUID doctorId,
        UUID hospitalId,
        UnavailabilityType unavailabilityType,
        LocalDate startDate,
        LocalDate endDate,
        boolean allDay,
        LocalTime startTime,
        LocalTime endTime,
        String reason,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
