package com.healthcare.hms.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.auth.crypto.TokenHashingService;
import com.healthcare.hms.auth.entity.RefreshToken;
import com.healthcare.hms.auth.repository.RefreshTokenRepository;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.common.exception.auth.EmailNotVerifiedException;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.security.jwt.JwtProperties;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenServiceImpl")
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenHashingService tokenHashingService;
    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = AuthTestData.activeVerifiedUser("hash");
        user.addRole(AuthTestData.hospitalAdminRole());
    }

    @Test
    @DisplayName("issueRefreshToken persists hashed token")
    void issueRefreshToken() {
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(7));
        when(refreshTokenRepository.findByUserIdAndRevokedFalseAndExpiresAtAfterOrderByCreatedAtAsc(eq(user.getId()), any()))
                .thenReturn(List.of());
        when(tokenHashingService.generateRawToken()).thenReturn("raw-refresh");
        when(tokenHashingService.hash("raw-refresh")).thenReturn("hash-refresh");

        final String raw = refreshTokenService.issueRefreshToken(user, "127.0.0.1", "junit");

        assertThat(raw).isEqualTo("raw-refresh");
        final ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("hash-refresh");
    }

    @Test
    @DisplayName("rotate returns new token for active refresh token")
    void rotateSuccess() {
        final RefreshToken existing = activeRefreshToken("old-hash");
        when(tokenHashingService.hash("old-raw")).thenReturn("old-hash");
        when(refreshTokenRepository.findByTokenHashWithUser("old-hash")).thenReturn(Optional.of(existing));
        when(userRepository.findByIdWithRolesAndPermissions(user.getId())).thenReturn(Optional.of(user));
        when(tokenHashingService.generateRawToken()).thenReturn("new-raw");
        when(tokenHashingService.hash("new-raw")).thenReturn("new-hash");
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(7));

        final var rotated = refreshTokenService.rotate("old-raw", "127.0.0.1", "junit");

        assertThat(rotated.newRefreshToken()).isEqualTo("new-raw");
        assertThat(rotated.user()).isEqualTo(user);
        assertThat(existing.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("rotate of revoked token revokes family and fails")
    void rotateReuse() {
        final RefreshToken existing = activeRefreshToken("old-hash");
        existing.revoke(null);
        when(tokenHashingService.hash("old-raw")).thenReturn("old-hash");
        when(refreshTokenRepository.findByTokenHashWithUser("old-hash")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> refreshTokenService.rotate("old-raw", "127.0.0.1", "junit"))
                .isInstanceOf(InvalidTokenException.class);
        verify(refreshTokenRepository).revokeAllActiveByUserId(eq(user.getId()), any());
    }

    @Test
    @DisplayName("rotate of expired token fails")
    void rotateExpired() {
        final RefreshToken existing = activeRefreshToken("old-hash");
        existing.setExpiresAt(Instant.now().minusSeconds(10));
        when(tokenHashingService.hash("old-raw")).thenReturn("old-hash");
        when(refreshTokenRepository.findByTokenHashWithUser("old-hash")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> refreshTokenService.rotate("old-raw", "127.0.0.1", "junit"))
                .isInstanceOf(ExpiredTokenException.class);
    }

    @Test
    @DisplayName("rotate rejects unverified email")
    void rotateUnverified() {
        user.setEmailVerified(false);
        final RefreshToken existing = activeRefreshToken("old-hash");
        when(tokenHashingService.hash("old-raw")).thenReturn("old-hash");
        when(refreshTokenRepository.findByTokenHashWithUser("old-hash")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> refreshTokenService.rotate("old-raw", "127.0.0.1", "junit"))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    @DisplayName("revokeAllForUser delegates to repository")
    void revokeAll() {
        when(refreshTokenRepository.revokeAllActiveByUserId(eq(user.getId()), any())).thenReturn(2);
        refreshTokenService.revokeAllForUser(user.getId());
        verify(refreshTokenRepository).revokeAllActiveByUserId(eq(user.getId()), any());
    }

    @Test
    @DisplayName("revokeToken revokes active token")
    void revokeToken() {
        final RefreshToken existing = activeRefreshToken("hash");
        when(tokenHashingService.hash("raw")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(existing));

        refreshTokenService.revokeToken("raw");

        assertThat(existing.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(existing);
    }

    private RefreshToken activeRefreshToken(final String hash) {
        final RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTenantId(user.getTenantId());
        token.setTokenHash(hash);
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        return token;
    }
}
