package com.healthcare.hms.security.jwt;

/**
 * Distinguishes access tokens from refresh tokens within JWT claims.
 */
public enum JwtTokenType {
    ACCESS,
    REFRESH
}
