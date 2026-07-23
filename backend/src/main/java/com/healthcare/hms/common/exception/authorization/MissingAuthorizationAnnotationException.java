package com.healthcare.hms.common.exception.authorization;

/**
 * Thrown when a controller handler is missing an authorization classification annotation.
 *
 * <p>Phase 3.8 fail-closed policy: every {@code /api/**} handler must declare
 * {@code @PublicEndpoint}, {@code @RequireAuthenticated}, {@code @RequirePermission},
 * or {@code @RequiresRole}.
 */
public class MissingAuthorizationAnnotationException extends AuthorizationException {

    public static final String ERROR_CODE = "AUTHZ_HANDLER_UNCLASSIFIED";

    public MissingAuthorizationAnnotationException(final String handlerSignature) {
        super(
                ERROR_CODE,
                "API handler is missing an authorization annotation: " + handlerSignature
        );
    }
}
