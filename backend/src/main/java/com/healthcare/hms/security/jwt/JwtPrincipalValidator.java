package com.healthcare.hms.security.jwt;

import com.healthcare.hms.common.exception.auth.AccountNotActiveException;
import com.healthcare.hms.common.exception.auth.EmailNotVerifiedException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validates JWT claims against the current user record (existence, status, token version).
 */
@Component
public class JwtPrincipalValidator {

    private final UserRepository userRepository;

    public JwtPrincipalValidator(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser validateAndBuildPrincipal(final JwtClaims claims) {
        final User user = userRepository.findById(claims.userId())
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

        if (claims.tenantId() != null
                && user.getTenantId() != null
                && !claims.tenantId().equals(user.getTenantId())) {
            throw new InvalidTokenException("Token tenant does not match the authenticated user");
        }

        return new AuthenticatedUser(
                claims.userId(),
                claims.tenantId(),
                claims.email(),
                claims.roles(),
                claims.permissions(),
                claims.tokenVersion()
        );
    }
}
