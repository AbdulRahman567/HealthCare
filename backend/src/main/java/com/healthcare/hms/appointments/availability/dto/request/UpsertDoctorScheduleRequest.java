package com.healthcare.hms.appointments.availability.dto.request;

import com.healthcare.hms.appointments.availability.enums.ScheduleRecurrenceType;
import com.healthcare.hms.appointments.availability.enums.ScheduleStatus;
import com.healthcare.hms.appointments.availability.validation.ValidDoctorScheduleRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Create / update recurring doctor schedule with working days, hours, and breaks.
 */
@ValidDoctorScheduleRequest
public record UpsertDoctorScheduleRequest(
        @Size(max = 150, message = "Schedule name must not exceed 150 characters")
        String name,

        @NotNull(message = "Effective from date is required")
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        @NotNull(message = "Maximum appointments per day is required")
        @Min(value = 1, message = "Maximum appointments per day must be at least 1")
        @Max(value = 500, message = "Maximum appointments per day must not exceed 500")
        Integer maxAppointmentsPerDay,

        ScheduleRecurrenceType recurrenceType,

        ScheduleStatus status,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes,

        @NotEmpty(message = "At least one working-hours window is required")
        @Valid
        List<ScheduleWindowRequest> windows,

        @Valid
        List<ScheduleBreakRequest> breaks
) {
}
