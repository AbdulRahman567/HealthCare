package com.healthcare.hms.common.persistence;

import com.healthcare.hms.security.principal.AuthenticatedUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the current auditor from the JWT-backed security context when present.
 */
@Component
public class SecurityAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            return Optional.empty();
        }
        return Optional.ofNullable(authenticatedUser.getUserId());
    }
}
