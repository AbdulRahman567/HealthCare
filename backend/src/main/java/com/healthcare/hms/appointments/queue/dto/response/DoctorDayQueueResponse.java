package com.healthcare.hms.appointments.queue.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DoctorDayQueueResponse(
        UUID id,
        UUID doctorId,
        UUID hospitalId,
        LocalDate queueDate,
        Integer lastQueueNumber,
        long waitingCount,
        long inConsultationCount,
        List<QueueEntryResponse> entries,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
