package com.healthcare.hms.security;

/**
 * Centralized security constants used across JWT processing, filters, and HTTP security.
 */
public final class SecurityConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TENANT_HEADER = "X-Tenant-ID";

    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String CLAIM_USER_ID = "user_id";
    public static final String CLAIM_TENANT_ID = "tenant_id";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_TOKEN_VERSION = "token_version";

    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * Request attribute used to surface JWT validation failures to the authentication entry point.
     */
    public static final String AUTH_EXCEPTION_ATTRIBUTE = "hms.security.authException";

    /**
     * Endpoints that remain publicly accessible without authentication.
     */
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/system/health",
            "/api/v1/auth/login",
            "/api/v1/auth/register/hospital",
            "/api/v1/auth/register/admin",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/resend-verification",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            // Scraped on the Docker network; do not expose port 8080 publicly in production.
            "/actuator/prometheus",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    private SecurityConstants() {
    }
}
