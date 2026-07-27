package com.healthcare.hms.appointments.queue.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Optional notes when updating a queue entry status.
 */
public record QueueStatusUpdateRequest(
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
