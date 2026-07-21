package com.healthcare.hms.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for rotating refresh tokens.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
