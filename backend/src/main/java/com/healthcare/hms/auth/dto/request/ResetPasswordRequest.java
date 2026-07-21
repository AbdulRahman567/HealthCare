package com.healthcare.hms.auth.dto.request;

import com.healthcare.hms.auth.validator.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Completes password recovery using a single-use reset token.
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Reset token is required")
        @Size(min = 32, max = 256, message = "Reset token is invalid")
        String token,

        @NotBlank(message = "New password is required")
        @StrongPassword
        String newPassword
) {
}
