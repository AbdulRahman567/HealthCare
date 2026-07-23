package com.healthcare.hms.security.util;

import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.security.principal.CurrentUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Stateless helpers for resolving the request-scoped authenticated principal.
 *
 * <p>Delegates to Spring's {@link SecurityContextHolder} (thread-local). Prefer
 * injecting {@link com.healthcare.hms.security.authorization.CurrentUserAccessor}
 * in Spring-managed beans for testability.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static boolean isAuthenticated() {
        return findCurrentUser().isPresent();
    }

    public static Optional<CurrentUser> findCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        final Object principal = authentication.getPrincipal();
        if (principal instanceof CurrentUser currentUser) {
            return Optional.of(currentUser);
        }
        return Optional.empty();
    }

    /**
     * @deprecated Prefer {@link #findCurrentUser()}; kept for call sites typed to {@link AuthenticatedUser}.
     */
    @Deprecated(forRemoval = false)
    public static Optional<AuthenticatedUser> findAuthenticatedUser() {
        return findCurrentUser()
                .filter(AuthenticatedUser.class::isInstance)
                .map(AuthenticatedUser.class::cast);
    }

    public static CurrentUser requireCurrentUser() {
        return findCurrentUser().orElseThrow(UnauthorizedException::new);
    }

    public static UUID requireCurrentUserId() {
        return requireCurrentUser().getUserId();
    }

    public static Optional<UUID> findCurrentTenantId() {
        return findCurrentUser().map(CurrentUser::getTenantId);
    }

    public static UUID requireCurrentTenantId() {
        final UUID tenantId = requireCurrentUser().getTenantId();
        if (tenantId == null) {
            throw new UnauthorizedException("Authenticated principal is not bound to a tenant");
        }
        return tenantId;
    }
}
