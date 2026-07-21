package com.healthcare.hms.auth.service.impl;

import com.healthcare.hms.auth.config.PasswordResetProperties;
import com.healthcare.hms.auth.crypto.TokenHashingService;
import com.healthcare.hms.auth.entity.PasswordResetToken;
import com.healthcare.hms.auth.repository.PasswordResetTokenRepository;
import com.healthcare.hms.auth.service.PasswordResetService;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.UserStatus;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Password-reset token lifecycle: issue, validate, single-use consume, secure expiration.
 */
@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenHashingService tokenHashingService;
    private final PasswordResetProperties passwordResetProperties;

    public PasswordResetServiceImpl(
            final PasswordResetTokenRepository passwordResetTokenRepository,
            final TokenHashingService tokenHashingService,
            final PasswordResetProperties passwordResetProperties
    ) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.tokenHashingService = tokenHashingService;
        this.passwordResetProperties = passwordResetProperties;
    }

    @Override
    public String issueResetToken(final User user, final String ipAddress, final String userAgent) {
        final Instant now = Instant.now();
        passwordResetTokenRepository.invalidateActiveTokensForUser(user.getId(), now, now);

        final String rawToken = tokenHashingService.generateRawToken();
        final PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTenantId(user.getTenantId());
        resetToken.setTokenHash(tokenHashingService.hash(rawToken));
        resetToken.setExpiresAt(now.plus(passwordResetProperties.getTokenExpiration()));
        resetToken.setIpAddress(truncate(ipAddress, 45));
        resetToken.setUserAgent(truncate(userAgent, 512));
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset token issued for userId={}", user.getId());
        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public User requireValidResetToken(final String rawToken) {
        return resolveActiveToken(rawToken).getUser();
    }

    @Override
    public void markTokenUsed(final String rawToken) {
        final PasswordResetToken resetToken = resolveActiveToken(rawToken);
        resetToken.markUsed();
        passwordResetTokenRepository.save(resetToken);
    }

    private PasswordResetToken resolveActiveToken(final String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Reset token is required");
        }

        final String tokenHash = tokenHashingService.hash(rawToken.trim());
        final PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHashWithUser(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Reset token is invalid or has already been used"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Reset token is invalid or has already been used");
        }

        if (resetToken.isExpired()) {
            throw new ExpiredTokenException("Reset token has expired");
        }

        final User user = resetToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidTokenException("User account is not active");
        }

        return resetToken;
    }

    private static String truncate(final String value, final int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
