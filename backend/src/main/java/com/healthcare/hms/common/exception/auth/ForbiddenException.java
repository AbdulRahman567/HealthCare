package com.healthcare.hms.common.exception.auth;

import com.healthcare.hms.common.exception.authorization.AuthorizationException;

/**
 * Generic authorization denial (HTTP 403).
 *
 * <p>Prefer {@link com.healthcare.hms.common.exception.authorization.PermissionDeniedException}
 * or {@link com.healthcare.hms.common.exception.authorization.RoleDeniedException} when the
 * failure reason is known. This type remains for broader “not allowed” cases.
 *
 * <p>Extends {@link AuthorizationException} (authZ), not {@link AuthenticationException} (authN).
 */
public class ForbiddenException extends AuthorizationException {

    public ForbiddenException() {
        super("AUTH_FORBIDDEN", "You do not have permission to access this resource");
    }

    public ForbiddenException(final String message) {
        super("AUTH_FORBIDDEN", message);
    }
}
