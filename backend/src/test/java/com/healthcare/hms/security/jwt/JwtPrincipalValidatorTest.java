package com.healthcare.hms.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.common.exception.auth.AccountNotActiveException;
import com.healthcare.hms.common.exception.auth.EmailNotVerifiedException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.authorization.PermissionResolver;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtPrincipalValidator")
class JwtPrincipalValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionResolver permissionResolver;

    @InjectMocks
    private JwtPrincipalValidator validator;

    @BeforeEach
    void stubPermissionResolver() {
        lenient().when(permissionResolver.resolveRoles(any(User.class))).thenAnswer(invocation -> {
            final User user = invocation.getArgument(0);
            return user.getRoles().stream()
                    .map(role -> role.getType().name())
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        });
        lenient().when(permissionResolver.resolvePermissions(any(User.class))).thenAnswer(invocation -> {
            final User user = invocation.getArgument(0);
            return user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getCode())
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        });
    }

    @Test
    @DisplayName("builds principal for active verified user from database roles")
    void validateAndBuildPrincipal_success() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.addRole(AuthTestData.hospitalAdminRole());
        when(userRepository.findByIdWithRolesAndPermissions(user.getId())).thenReturn(Optional.of(user));

        final JwtClaims claims = new JwtClaims(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                Set.of("STALE_ROLE"),
                Set.of("STALE_PERMISSION"),
                0L,
                JwtTokenType.ACCESS
        );

        final AuthenticatedUser principal = validator.validateAndBuildPrincipal(claims);

        assertThat(principal.getUserId()).isEqualTo(user.getId());
        assertThat(principal.getTenantId()).isEqualTo(user.getTenantId());
        assertThat(principal.getEmail()).isEqualTo(user.getEmail());
        assertThat(principal.getRoles()).containsExactly("HOSPITAL_ADMIN");
        assertThat(principal.getPermissions()).contains("HOSPITAL_READ");
        assertThat(principal.getRoles()).doesNotContain("STALE_ROLE");
        assertThat(principal.getPermissions()).doesNotContain("STALE_PERMISSION");
    }

    @Test
    @DisplayName("claim tenant mismatch against database rejects the token")
    void tenantClaimMismatch() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        when(userRepository.findByIdWithRolesAndPermissions(user.getId())).thenReturn(Optional.of(user));

        final JwtClaims claims = new JwtClaims(
                user.getId(),
                java.util.UUID.randomUUID(),
                user.getEmail(),
                Set.of("HOSPITAL_ADMIN"),
                Set.of(),
                0L,
                JwtTokenType.ACCESS
        );

        assertThatThrownBy(() -> validator.validateAndBuildPrincipal(claims))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("tenant");
    }

    @Test
    @DisplayName("missing user rejects token")
    void missingUser() {
        final java.util.UUID userId = AuthTestData.userId();
        when(userRepository.findByIdWithRolesAndPermissions(userId)).thenReturn(Optional.empty());

        final JwtClaims claims = new JwtClaims(
                userId,
                AuthTestData.tenantId(),
                "gone@hospital.test",
                Set.of(),
                Set.of(),
                0L,
                JwtTokenType.ACCESS
        );

        assertThatThrownBy(() -> validator.validateAndBuildPrincipal(claims))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("inactive user is rejected")
    void inactiveUser() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByIdWithRolesAndPermissions(user.getId())).thenReturn(Optional.of(user));

        final JwtClaims claims = new JwtClaims(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                Set.of(),
                Set.of(),
                0L,
                JwtTokenType.ACCESS
        );

        assertThatThrownBy(() -> validator.validateAndBuildPrincipal(claims))
                .isInstanceOf(AccountNotActiveException.class);
    }

    @Test
    @DisplayName("unverified email is rejected")
    void unverifiedEmail() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setEmailVerified(false);
        when(userRepository.findByIdWithRolesAndPermissions(user.getId())).thenReturn(Optional.of(user));

        final JwtClaims claims = new JwtClaims(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                Set.of(),
                Set.of(),
                0L,
                JwtTokenType.ACCESS
        );

        assertThatThrownBy(() -> validator.validateAndBuildPrincipal(claims))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    @DisplayName("token version mismatch is rejected")
    void tokenVersionMismatch() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setTokenVersion(2L);
        when(userRepository.findByIdWithRolesAndPermissions(user.getId())).thenReturn(Optional.of(user));

        final JwtClaims claims = new JwtClaims(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                Set.of(),
                Set.of(),
                0L,
                JwtTokenType.ACCESS
        );

        assertThatThrownBy(() -> validator.validateAndBuildPrincipal(claims))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("revoked");
    }
}
