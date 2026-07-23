package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.principal.CurrentUser;
import com.healthcare.hms.security.util.SecurityUtils;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * {@link CurrentUserAccessor} backed by Spring Security's thread-local context.
 */
@Component
public class SecurityContextCurrentUserAccessor implements CurrentUserAccessor {

    @Override
    public Optional<CurrentUser> findCurrentUser() {
        return SecurityUtils.findCurrentUser();
    }
}
