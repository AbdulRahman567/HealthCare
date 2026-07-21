package com.healthcare.hms.auth.dto.response;

/**
 * Authentication success payload with access and refresh tokens.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        long refreshExpiresInSeconds,
        UserProfileResponse user
) {
}
