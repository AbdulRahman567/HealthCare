package com.healthcare.hms.users.dto.response;

import com.healthcare.hms.users.enums.RoleType;
import java.time.Instant;

/**
 * Public invitation preview — intentionally omits tenant/hospital/inviter identifiers.
 */
public record InvitationPreviewResponse(
        String email,
        String firstName,
        String lastName,
        RoleType roleType,
        String hospitalName,
        Instant expiresAt,
        boolean expired
) {
}
