package com.healthcare.hms.users.dto.response;

import com.healthcare.hms.users.enums.InvitationStatus;
import com.healthcare.hms.users.enums.RoleType;
import java.time.Instant;
import java.util.UUID;

/**
 * User invitation API response (never includes the raw token).
 */
public record UserInvitationResponse(
        UUID id,
        UUID tenantId,
        UUID hospitalId,
        String email,
        String firstName,
        String lastName,
        RoleType roleType,
        UUID invitedBy,
        InvitationStatus status,
        Instant expiresAt,
        Instant acceptedAt,
        Instant rejectedAt,
        Instant cancelledAt,
        UUID acceptedUserId,
        String message,
        boolean expired,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
