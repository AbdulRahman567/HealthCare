package com.healthcare.hms.clinical.followup.dto.request;

import com.healthcare.hms.clinical.enums.FollowUpStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Explicit status transition for a follow-up plan.
 */
public record UpdateFollowUpStatusRequest(
        @NotNull(message = "Status is required")
        FollowUpStatus status
) {
}
