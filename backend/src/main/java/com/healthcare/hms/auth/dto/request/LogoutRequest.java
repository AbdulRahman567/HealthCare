package com.healthcare.hms.auth.dto.request;

/**
 * Optional logout payload used to revoke a specific refresh token in addition to all sessions.
 */
public record LogoutRequest(
        String refreshToken
) {
}
