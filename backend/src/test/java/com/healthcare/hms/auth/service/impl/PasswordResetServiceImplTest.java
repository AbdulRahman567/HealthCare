package com.healthcare.hms.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.auth.config.PasswordResetProperties;
import com.healthcare.hms.auth.crypto.TokenHashingService;
import com.healthcare.hms.auth.entity.PasswordResetToken;
import com.healthcare.hms.auth.repository.PasswordResetTokenRepository;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.users.entity.User;
import java.time.Duration;
import java.time.Instant;
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
@DisplayName("PasswordResetServiceImpl")
class PasswordResetServiceImplTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private TokenHashingService tokenHashingService;
    @Mock
    private PasswordResetProperties passwordResetProperties;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = AuthTestData.activeVerifiedUser("hash");
    }

    @Test
    @DisplayName("issueResetToken invalidates prior tokens and persists hash")
    void issueResetToken() {
        when(passwordResetProperties.getTokenExpiration()).thenReturn(Duration.ofHours(1));
        when(tokenHashingService.generateRawToken()).thenReturn("raw-reset-token");
        when(tokenHashingService.hash("raw-reset-token")).thenReturn("hashed-reset-token");

        final String raw = passwordResetService.issueResetToken(user, "127.0.0.1", "junit");

        assertThat(raw).isEqualTo("raw-reset-token");
        verify(passwordResetTokenRepository).invalidateActiveTokensForUser(eq(user.getId()), any(), any());
        final ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("hashed-reset-token");
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("requireValidResetToken returns user for active token")
    void requireValidResetToken() {
        final PasswordResetToken token = activeToken();
        when(tokenHashingService.hash("raw")).thenReturn("hash");
        when(passwordResetTokenRepository.findByTokenHashWithUser("hash")).thenReturn(Optional.of(token));

        assertThat(passwordResetService.requireValidResetToken("raw")).isEqualTo(user);
    }

    @Test
    @DisplayName("blank token is rejected")
    void blankToken() {
        assertThatThrownBy(() -> passwordResetService.requireValidResetToken(" "))
                .isInstanceOf(InvalidTokenException.class);
        verify(passwordResetTokenRepository, never()).findByTokenHashWithUser(anyString());
    }

    @Test
    @DisplayName("expired token throws ExpiredTokenException")
    void expiredToken() {
        final PasswordResetToken token = activeToken();
        token.setExpiresAt(Instant.now().minusSeconds(60));
        when(tokenHashingService.hash("raw")).thenReturn("hash");
        when(passwordResetTokenRepository.findByTokenHashWithUser("hash")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.requireValidResetToken("raw"))
                .isInstanceOf(ExpiredTokenException.class);
    }

    @Test
    @DisplayName("used token throws InvalidTokenException")
    void usedToken() {
        final PasswordResetToken token = activeToken();
        token.markUsed();
        when(tokenHashingService.hash("raw")).thenReturn("hash");
        when(passwordResetTokenRepository.findByTokenHashWithUser("hash")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.requireValidResetToken("raw"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("markTokenUsed persists usedAt")
    void markTokenUsed() {
        final PasswordResetToken token = activeToken();
        when(tokenHashingService.hash("raw")).thenReturn("hash");
        when(passwordResetTokenRepository.findByTokenHashWithUser("hash")).thenReturn(Optional.of(token));

        passwordResetService.markTokenUsed("raw");

        assertThat(token.isUsed()).isTrue();
        verify(passwordResetTokenRepository).save(token);
    }

    private PasswordResetToken activeToken() {
        final PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTenantId(user.getTenantId());
        token.setTokenHash("hash");
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        return token;
    }
}
