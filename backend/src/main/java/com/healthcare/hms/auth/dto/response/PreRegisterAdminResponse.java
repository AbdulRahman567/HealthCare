package com.healthcare.hms.auth.dto.response;

/**
 * Response returned after step 1 of hospital onboarding.
 * The client must present this token when completing the registration in step 2.
 */
public record PreRegisterAdminResponse(
        String registrationToken,
        String email,
        int expiresInMinutes
) {
}
