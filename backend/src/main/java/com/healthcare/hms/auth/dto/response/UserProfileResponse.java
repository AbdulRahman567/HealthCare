package com.healthcare.hms.auth.dto.response;

import com.healthcare.hms.users.enums.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Authenticated user profile representation.
 */
public record UserProfileResponse(
        UUID id,
        UUID tenantId,
        String firstName,
        String lastName,
        String email,
        String phone,
        boolean emailVerified,
        Instant emailVerifiedAt,
        UserStatus status,
        Set<String> roles,
        Set<String> permissions,
        Instant lastLoginAt,
        Instant createdAt
) {
}
