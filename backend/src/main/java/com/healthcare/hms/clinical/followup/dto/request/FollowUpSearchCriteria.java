package com.healthcare.hms.clinical.followup.dto.request;

import com.healthcare.hms.clinical.enums.FollowUpPriority;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Optimized search criteria for follow-up due lists and tenant search.
 */
public record FollowUpSearchCriteria(
        UUID patientId,
        UUID doctorId,
        UUID consultationId,
        FollowUpStatus status,
        FollowUpPriority priority,
        LocalDate fromDate,
        LocalDate toDate,
        Boolean overdueOnly,
        Boolean dueSoonOnly,
        Integer dueWithinDays
) {
}
