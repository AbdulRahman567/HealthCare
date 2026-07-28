package com.healthcare.hms.appointments.queue.dto.response;

import com.healthcare.hms.appointments.queue.enums.QueueEntryStatus;
import java.time.Instant;
import java.util.UUID;

public record QueueEntryResponse(
        UUID id,
        UUID queueId,
        UUID appointmentId,
        UUID patientId,
        String patientName,
        UUID doctorId,
        UUID hospitalId,
        Integer queueNumber,
        QueueEntryStatus status,
        Instant checkedInAt,
        Instant statusChangedAt,
        String notes,
        /** Linked clinical consultation when present (Phase 7.10). */
        UUID consultationId,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
