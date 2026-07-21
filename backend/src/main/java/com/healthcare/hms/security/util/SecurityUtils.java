package com.healthcare.hms.security.util;

import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helpers for resolving the current authenticated principal.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AuthenticatedUser> findCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            return Optional.empty();
        }
        return Optional.of(authenticatedUser);
    }

    public static AuthenticatedUser requireCurrentUser() {
        return findCurrentUser().orElseThrow(UnauthorizedException::new);
    }
}
