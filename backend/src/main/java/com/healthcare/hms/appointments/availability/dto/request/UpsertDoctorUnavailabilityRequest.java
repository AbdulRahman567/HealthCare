package com.healthcare.hms.appointments.availability.dto.request;

import com.healthcare.hms.appointments.availability.enums.UnavailabilityType;
import com.healthcare.hms.appointments.availability.validation.ValidDoctorUnavailabilityRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Create / update leave, holiday, or emergency unavailability.
 */
@ValidDoctorUnavailabilityRequest
public record UpsertDoctorUnavailabilityRequest(
        @NotNull(message = "Unavailability type is required")
        UnavailabilityType unavailabilityType,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotNull(message = "All-day flag is required")
        Boolean allDay,

        LocalTime startTime,

        LocalTime endTime,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
