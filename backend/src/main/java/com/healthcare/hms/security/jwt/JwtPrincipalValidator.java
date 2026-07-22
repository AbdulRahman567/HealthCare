package com.healthcare.hms.security.jwt;

import com.healthcare.hms.common.exception.auth.AccountNotActiveException;
import com.healthcare.hms.common.exception.auth.EmailNotVerifiedException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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

    public JwtPrincipalValidator(final UserRepository userRepository) {
        this.userRepository = userRepository;
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
                extractRoles(user),
                extractPermissions(user),
                claims.tokenVersion()
        );
    }

    private static Set<String> extractRoles(final User user) {
        return user.getRoles().stream()
                .map(Role::getType)
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> extractPermissions(final User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }
}
