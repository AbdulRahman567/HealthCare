package com.healthcare.hms.clinical.followup.dto.request;

import com.healthcare.hms.clinical.enums.FollowUpPriority;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
import com.healthcare.hms.clinical.followup.validation.ValidFollowUpSchedule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Update a follow-up plan (plan fields require editable consultation or open status).
 */
@ValidFollowUpSchedule
public record UpdateFollowUpRequest(
        LocalDate scheduledDate,

        LocalTime scheduledTime,

        FollowUpStatus status,

        FollowUpPriority priority,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason,

        @Size(max = 1000, message = "Instructions must not exceed 1000 characters")
        String instructions,

        @Size(max = 2000, message = "Clinical recommendations must not exceed 2000 characters")
        String clinicalRecommendations,

        UUID doctorId,

        UUID followUpAppointmentId,

        Boolean reminderEnabled,

        @Min(value = 0, message = "Reminder lead days must be at least 0")
        @Max(value = 90, message = "Reminder lead days must not exceed 90")
        Integer reminderLeadDays
) {
}
