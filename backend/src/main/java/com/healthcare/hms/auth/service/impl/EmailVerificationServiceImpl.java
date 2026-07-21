package com.healthcare.hms.auth.service.impl;

import com.healthcare.hms.auth.config.EmailVerificationProperties;
import com.healthcare.hms.auth.crypto.TokenHashingService;
import com.healthcare.hms.auth.entity.EmailVerificationToken;
import com.healthcare.hms.auth.repository.EmailVerificationTokenRepository;
import com.healthcare.hms.auth.service.EmailVerificationService;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.users.entity.User;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Email-verification token lifecycle: issue, validate, single-use consume, secure expiration.
 */
@Service
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final TokenHashingService tokenHashingService;
    private final EmailVerificationProperties emailVerificationProperties;

    public EmailVerificationServiceImpl(
            final EmailVerificationTokenRepository emailVerificationTokenRepository,
            final TokenHashingService tokenHashingService,
            final EmailVerificationProperties emailVerificationProperties
    ) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.tokenHashingService = tokenHashingService;
        this.emailVerificationProperties = emailVerificationProperties;
    }

    @Override
    public String issueVerificationToken(final User user, final String ipAddress, final String userAgent) {
        final Instant now = Instant.now();
        emailVerificationTokenRepository.invalidateActiveTokensForUser(user.getId(), now, now);

        final String rawToken = tokenHashingService.generateRawToken();
        final EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setTenantId(user.getTenantId());
        verificationToken.setTokenHash(tokenHashingService.hash(rawToken));
        verificationToken.setExpiresAt(now.plus(emailVerificationProperties.getTokenExpiration()));
        verificationToken.setIpAddress(truncate(ipAddress, 45));
        verificationToken.setUserAgent(truncate(userAgent, 512));
        emailVerificationTokenRepository.save(verificationToken);

        log.info("Email verification token issued for userId={}", user.getId());
        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public User requireValidVerificationToken(final String rawToken) {
        return resolveActiveToken(rawToken).getUser();
    }

    @Override
    public void markTokenUsed(final String rawToken) {
        final EmailVerificationToken verificationToken = resolveActiveToken(rawToken);
        verificationToken.markUsed();
        emailVerificationTokenRepository.save(verificationToken);
    }

    private EmailVerificationToken resolveActiveToken(final String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Verification token is required");
        }

        final String tokenHash = tokenHashingService.hash(rawToken.trim());
        final EmailVerificationToken verificationToken =
                emailVerificationTokenRepository.findByTokenHashWithUser(tokenHash)
                        .orElseThrow(() -> new InvalidTokenException(
                                "Verification token is invalid or has already been used"
                        ));

        if (verificationToken.isUsed()) {
            throw new InvalidTokenException("Verification token is invalid or has already been used");
        }

        if (verificationToken.isExpired()) {
            throw new ExpiredTokenException("Verification token has expired");
        }

        final User user = verificationToken.getUser();
        if (user.isEmailVerified()) {
            throw new InvalidTokenException("Email address is already verified");
        }

        return verificationToken;
    }

    private static String truncate(final String value, final int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
