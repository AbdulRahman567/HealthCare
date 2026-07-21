package com.healthcare.hms.security.jwt;

import java.util.Set;
import java.util.UUID;

/**
 * Immutable JWT claim payload used for token creation and validation.
 */
public record JwtClaims(
        UUID userId,
        UUID tenantId,
        String email,
        Set<String> roles,
        Set<String> permissions,
        long tokenVersion,
        JwtTokenType tokenType
) {
}
