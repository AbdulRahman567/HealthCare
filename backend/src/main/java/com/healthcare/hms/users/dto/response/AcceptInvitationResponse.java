package com.healthcare.hms.users.dto.response;

import com.healthcare.hms.users.enums.RoleType;
import java.util.UUID;

/**
 * Result of accepting an invitation (account created and role assigned).
 */
public record AcceptInvitationResponse(
        UUID invitationId,
        UUID userId,
        UUID tenantId,
        UUID hospitalId,
        String email,
        RoleType roleType,
        String message
) {
}
