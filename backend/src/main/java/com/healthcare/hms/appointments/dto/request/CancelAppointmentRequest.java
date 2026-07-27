package com.healthcare.hms.appointments.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Cancel a scheduled or confirmed appointment.
 */
public record CancelAppointmentRequest(
        @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
        String reason
) {
}
