package com.healthcare.hms.users.dto.response;

import com.healthcare.hms.users.enums.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Tenant user management response (Phase 4.6).
 */
public record UserManagementResponse(
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
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy,
        UUID updatedBy,
        Long version
) {
}
