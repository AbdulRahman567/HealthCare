package com.healthcare.hms.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.common.exception.auth.AccountNotActiveException;
import com.healthcare.hms.common.exception.auth.EmailNotVerifiedException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
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

    @InjectMocks
    private JwtPrincipalValidator validator;

    @Test
    @DisplayName("builds principal for active verified user")
    void validateAndBuildPrincipal_success() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        final JwtClaims claims = new JwtClaims(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                Set.of("HOSPITAL_ADMIN"),
                Set.of("HOSPITAL_READ"),
                0L,
                JwtTokenType.ACCESS
        );

        final AuthenticatedUser principal = validator.validateAndBuildPrincipal(claims);

        assertThat(principal.getUserId()).isEqualTo(user.getId());
        assertThat(principal.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("missing user throws UnauthorizedException")
    void missingUser() {
        when(userRepository.findById(AuthTestData.userId())).thenReturn(Optional.empty());

        final JwtClaims claims = new JwtClaims(
                AuthTestData.userId(),
                AuthTestData.tenantId(),
                "admin@hospital.test",
                Set.of(),
                Set.of(),
                0L,
                JwtTokenType.ACCESS
        );

        assertThatThrownBy(() -> validator.validateAndBuildPrincipal(claims))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("inactive account throws AccountNotActiveException")
    void inactiveAccount() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

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
    @DisplayName("unverified email throws EmailNotVerifiedException")
    void unverifiedEmail() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setEmailVerified(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

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
    @DisplayName("token version mismatch throws InvalidTokenException")
    void tokenVersionMismatch() {
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setTokenVersion(2L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

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
                .isInstanceOf(InvalidTokenException.class);
    }
}
