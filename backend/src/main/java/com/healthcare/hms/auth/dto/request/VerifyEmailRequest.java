package com.healthcare.hms.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Completes email verification using a single-use token from the verification email.
 */
public record VerifyEmailRequest(
        @NotBlank(message = "Verification token is required")
        @Size(min = 32, max = 256, message = "Verification token is invalid")
        String token
) {
}
