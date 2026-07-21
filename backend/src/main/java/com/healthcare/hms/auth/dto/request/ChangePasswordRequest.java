package com.healthcare.hms.auth.dto.request;

import com.healthcare.hms.auth.validator.StrongPassword;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for changing the authenticated user's password.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @StrongPassword
        String newPassword
) {
}
