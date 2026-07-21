package com.healthcare.hms.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.auth.config.EmailVerificationProperties;
import com.healthcare.hms.auth.crypto.TokenHashingService;
import com.healthcare.hms.auth.entity.EmailVerificationToken;
import com.healthcare.hms.auth.repository.EmailVerificationTokenRepository;
import com.healthcare.hms.auth.support.AuthTestData;
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
@DisplayName("EmailVerificationServiceImpl")
class EmailVerificationServiceImplTest {

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock
    private TokenHashingService tokenHashingService;
    @Mock
    private EmailVerificationProperties emailVerificationProperties;

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = AuthTestData.activeVerifiedUser("hash");
        user.setEmailVerified(false);
    }

    @Test
    @DisplayName("issueVerificationToken persists hashed token")
    void issueVerificationToken() {
        when(emailVerificationProperties.getTokenExpiration()).thenReturn(Duration.ofHours(24));
        when(tokenHashingService.generateRawToken()).thenReturn("raw-verify");
        when(tokenHashingService.hash("raw-verify")).thenReturn("hash-verify");

        final String raw = emailVerificationService.issueVerificationToken(user, "127.0.0.1", "junit");

        assertThat(raw).isEqualTo("raw-verify");
        verify(emailVerificationTokenRepository).invalidateActiveTokensForUser(eq(user.getId()), any(), any());
        final ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailVerificationTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("hash-verify");
    }

    @Test
    @DisplayName("requireValidVerificationToken returns unverified user")
    void requireValid() {
        final EmailVerificationToken token = activeToken();
        when(tokenHashingService.hash("raw")).thenReturn("hash");
        when(emailVerificationTokenRepository.findByTokenHashWithUser("hash")).thenReturn(Optional.of(token));

        assertThat(emailVerificationService.requireValidVerificationToken("raw")).isEqualTo(user);
    }

    @Test
    @DisplayName("already verified user is rejected")
    void alreadyVerified() {
        user.setEmailVerified(true);
        final EmailVerificationToken token = activeToken();
        when(tokenHashingService.hash("raw")).thenReturn("hash");
        when(emailVerificationTokenRepository.findByTokenHashWithUser("hash")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> emailVerificationService.requireValidVerificationToken("raw"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("markTokenUsed persists usedAt")
    void markUsed() {
        final EmailVerificationToken token = activeToken();
        when(tokenHashingService.hash("raw")).thenReturn("hash");
        when(emailVerificationTokenRepository.findByTokenHashWithUser("hash")).thenReturn(Optional.of(token));

        emailVerificationService.markTokenUsed("raw");

        assertThat(token.isUsed()).isTrue();
        verify(emailVerificationTokenRepository).save(token);
    }

    private EmailVerificationToken activeToken() {
        final EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTenantId(user.getTenantId());
        token.setTokenHash("hash");
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        return token;
    }
}
