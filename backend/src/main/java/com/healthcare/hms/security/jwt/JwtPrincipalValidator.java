package com.healthcare.hms.security.jwt;

import com.healthcare.hms.common.exception.auth.AccountNotActiveException;
import com.healthcare.hms.common.exception.auth.EmailNotVerifiedException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.authorization.PermissionResolver;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validates JWT claims against the current user record (existence, status, token version).
 *
 * <p>Tenant identity, roles, and permissions on the principal are taken from the database,
 * not the JWT claims, so stale or crafted claims cannot escalate privileges. The access
 * token remains a bearer of user id + token version; authorization state is always fresh.
 */
@Component
public class JwtPrincipalValidator {

    private final UserRepository userRepository;
    private final PermissionResolver permissionResolver;

    public JwtPrincipalValidator(
            final UserRepository userRepository,
            final PermissionResolver permissionResolver
    ) {
        this.userRepository = userRepository;
        this.permissionResolver = permissionResolver;
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser validateAndBuildPrincipal(final JwtClaims claims) {
        final User user = userRepository.findByIdWithRolesAndPermissions(claims.userId())
                .orElseThrow(() -> new UnauthorizedException("User linked to token no longer exists"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        if (user.getTokenVersion() != claims.tokenVersion()) {
            throw new InvalidTokenException("Access token has been revoked");
        }

        // Strict equality — including null vs non-null — prevents claim/DB drift escalation.
        if (!Objects.equals(claims.tenantId(), user.getTenantId())) {
            throw new InvalidTokenException("Token tenant does not match the authenticated user");
        }

        return new AuthenticatedUser(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                permissionResolver.resolveRoles(user),
                permissionResolver.resolvePermissions(user),
                claims.tokenVersion()
        );
    }
}
