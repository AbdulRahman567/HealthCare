package com.healthcare.hms.appointments.dto.request;

import com.healthcare.hms.appointments.validation.ValidAppointmentSlotRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Move an existing bookable appointment to a new slot (and optionally doctor/department).
 * Confirmation is cleared; patient remains the same.
 */
@ValidAppointmentSlotRequest
public record RescheduleAppointmentRequest(
        @NotNull(message = "Doctor id is required")
        UUID doctorId,

        @NotNull(message = "Department id is required")
        UUID departmentId,

        @NotNull(message = "Appointment date is required")
        LocalDate appointmentDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @NotNull(message = "Duration is required")
        @Min(value = 5, message = "Duration must be at least 5 minutes")
        @Max(value = 480, message = "Duration must not exceed 480 minutes")
        Integer durationMinutes
) {
}
