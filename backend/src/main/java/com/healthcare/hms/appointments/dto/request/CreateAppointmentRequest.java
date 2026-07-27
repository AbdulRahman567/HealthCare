package com.healthcare.hms.appointments.dto.request;

import com.healthcare.hms.appointments.enums.AppointmentType;
import com.healthcare.hms.appointments.enums.VisitType;
import com.healthcare.hms.appointments.validation.ValidAppointmentSlotRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Book a new appointment for an active patient with an active doctor.
 */
@ValidAppointmentSlotRequest
public record CreateAppointmentRequest(
        @NotNull(message = "Patient id is required")
        UUID patientId,

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
        Integer durationMinutes,

        @NotNull(message = "Appointment type is required")
        AppointmentType appointmentType,

        @NotNull(message = "Visit type is required")
        VisitType visitType,

        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        String notes
) {
}
