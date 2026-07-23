package com.healthcare.hms.security.authorization;

import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.principal.CurrentUser;
import java.util.Optional;

/**
 * Resolves the authenticated {@link CurrentUser} for the current request.
 *
 * <p>Injectable alternative to static {@link com.healthcare.hms.security.util.SecurityUtils}
 * for cleaner unit testing.
 */
public interface CurrentUserAccessor {

    Optional<CurrentUser> findCurrentUser();

    default CurrentUser requireCurrentUser() {
        return findCurrentUser().orElseThrow(UnauthorizedException::new);
    }

    default boolean isAuthenticated() {
        return findCurrentUser().isPresent();
    }
}
