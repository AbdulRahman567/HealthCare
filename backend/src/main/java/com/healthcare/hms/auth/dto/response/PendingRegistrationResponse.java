package com.healthcare.hms.auth.dto.response;

/**
 * Response after a pending registration is submitted or its verification email resent.
 */
public record PendingRegistrationResponse(
        String email,
        int expiresInMinutes
) {
}
