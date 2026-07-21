package com.healthcare.hms.auth.service;

import com.healthcare.hms.users.entity.User;

/**
 * Issues, rotates, and revokes refresh tokens with secure hashing.
 */
public interface RefreshTokenService {

    /**
     * Creates a new refresh token for the user and returns the raw token value.
     */
    String issueRefreshToken(User user, String ipAddress, String userAgent);

    /**
     * Rotates a valid refresh token and returns the new raw refresh token.
     * Detects reuse of revoked tokens and invalidates the user's token family.
     */
    RotatedTokens rotate(String rawRefreshToken, String ipAddress, String userAgent);

    /**
     * Revokes every active refresh token for the user (logout / password change).
     */
    void revokeAllForUser(java.util.UUID userId);

    /**
     * Revokes a single refresh token if it is still active.
     */
    void revokeToken(String rawRefreshToken);

    record RotatedTokens(User user, String newRefreshToken) {
    }
}
