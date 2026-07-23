package com.healthcare.hms.security.authorization;

/**
 * Canonical access-denied payloads for permission-based authorization (Phase 3.3).
 *
 * <p>Keeps filter-chain ({@code AccessDeniedHandler}) and MVC
 * ({@code @ControllerAdvice}) responses aligned without leaking permission codes.
 */
public final class AccessDeniedResponses {

    public static final String ERROR_CODE = "AUTHZ_ACCESS_DENIED";
    public static final String MESSAGE = "You do not have permission to access this resource";

    private AccessDeniedResponses() {
    }
}
