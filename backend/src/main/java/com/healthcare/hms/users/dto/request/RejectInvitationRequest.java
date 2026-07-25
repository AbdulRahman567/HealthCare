package com.healthcare.hms.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Reject an invitation using the emailed token.
 */
public record RejectInvitationRequest(
        @NotBlank(message = "Invitation token is required")
        @Size(min = 32, max = 256, message = "Invitation token is invalid")
        String token
) {
}
