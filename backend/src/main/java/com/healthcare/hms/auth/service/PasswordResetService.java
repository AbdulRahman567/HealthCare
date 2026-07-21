package com.healthcare.hms.auth.service;

import com.healthcare.hms.users.entity.User;

/**
 * Issues and consumes password-reset tokens with secure hashing and expiration.
 */
public interface PasswordResetService {

    /**
     * Creates a reset token for an active user and returns the raw token value
     * (only for building the email link — never persist the raw value).
     */
    String issueResetToken(User user, String ipAddress, String userAgent);

    /**
     * Validates a raw reset token without consuming it and returns the associated user.
     */
    User requireValidResetToken(String rawToken);

    /**
     * Marks a previously validated reset token as used (single-use enforcement).
     */
    void markTokenUsed(String rawToken);
}
