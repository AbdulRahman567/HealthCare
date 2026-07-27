package com.healthcare.hms.appointments.queue.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Check a booked appointment into the doctor's daily queue.
 */
public record CheckInQueueRequest(
        @NotNull(message = "Appointment id is required")
        UUID appointmentId,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
