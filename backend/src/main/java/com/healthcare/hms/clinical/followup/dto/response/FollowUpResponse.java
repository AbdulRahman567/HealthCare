package com.healthcare.hms.clinical.followup.dto.response;

import com.healthcare.hms.clinical.enums.FollowUpPriority;
import com.healthcare.hms.clinical.enums.FollowUpReminderStatus;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Consultation follow-up plan snapshot.
 */
public record FollowUpResponse(
        UUID id,
        UUID consultationId,
        String consultationNumber,
        UUID patientId,
        UUID doctorId,
        String doctorName,
        LocalDate scheduledDate,
        LocalTime scheduledTime,
        FollowUpStatus status,
        FollowUpPriority priority,
        String reason,
        String instructions,
        String clinicalRecommendations,
        UUID followUpAppointmentId,
        Boolean reminderEnabled,
        Integer reminderLeadDays,
        Instant nextReminderAt,
        Instant lastReminderAt,
        FollowUpReminderStatus reminderStatus,
        Instant createdAt,
        Long version
) {
}
