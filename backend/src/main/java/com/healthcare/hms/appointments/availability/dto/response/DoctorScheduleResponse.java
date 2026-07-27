package com.healthcare.hms.appointments.availability.dto.response;

import com.healthcare.hms.appointments.availability.enums.ScheduleRecurrenceType;
import com.healthcare.hms.appointments.availability.enums.ScheduleStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DoctorScheduleResponse(
        UUID id,
        UUID doctorId,
        UUID hospitalId,
        String name,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Integer maxAppointmentsPerDay,
        ScheduleRecurrenceType recurrenceType,
        ScheduleStatus status,
        String notes,
        List<ScheduleWindowResponse> windows,
        List<ScheduleBreakResponse> breaks,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
