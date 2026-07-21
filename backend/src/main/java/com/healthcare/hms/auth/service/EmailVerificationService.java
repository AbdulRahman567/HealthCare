package com.healthcare.hms.auth.service;

import com.healthcare.hms.users.entity.User;

/**
 * Issues and consumes email-verification tokens with secure hashing and expiration.
 */
public interface EmailVerificationService {

    /**
     * Creates a verification token for an unverified user and returns the raw token value
     * (only for building the email link — never persist the raw value).
     */
    String issueVerificationToken(User user, String ipAddress, String userAgent);

    /**
     * Validates a raw verification token without consuming it and returns the associated user.
     */
    User requireValidVerificationToken(String rawToken);

    /**
     * Marks a previously validated verification token as used (single-use enforcement).
     */
    void markTokenUsed(String rawToken);
}
