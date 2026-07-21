package com.healthcare.hms.auth.service.impl;

import com.healthcare.hms.auth.crypto.TokenHashingService;
import com.healthcare.hms.auth.entity.RefreshToken;
import com.healthcare.hms.auth.repository.RefreshTokenRepository;
import com.healthcare.hms.auth.service.RefreshTokenService;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.common.exception.auth.EmailNotVerifiedException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.security.jwt.JwtProperties;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh-token lifecycle with rotation, reuse detection, and session limits.
 */
@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);
    private static final int MAX_ACTIVE_SESSIONS = 5;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenHashingService tokenHashingService;
    private final JwtProperties jwtProperties;

    public RefreshTokenServiceImpl(
            final RefreshTokenRepository refreshTokenRepository,
            final UserRepository userRepository,
            final TokenHashingService tokenHashingService,
            final JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenHashingService = tokenHashingService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String issueRefreshToken(final User user, final String ipAddress, final String userAgent) {
        enforceSessionLimit(user.getId());

        final String rawToken = tokenHashingService.generateRawToken();
        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTenantId(user.getTenantId());
        refreshToken.setTokenHash(tokenHashingService.hash(rawToken));
        refreshToken.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpiration()));
        refreshToken.setIpAddress(truncate(ipAddress, 45));
        refreshToken.setUserAgent(truncate(userAgent, 512));
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Override
    public RotatedTokens rotate(final String rawRefreshToken, final String ipAddress, final String userAgent) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token is required");
        }

        final String tokenHash = tokenHashingService.hash(rawRefreshToken.trim());
        final RefreshToken existing = refreshTokenRepository.findByTokenHashWithUser(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));

        if (existing.isRevoked()) {
            // Reuse of a rotated/revoked token indicates possible theft — revoke the family.
            refreshTokenRepository.revokeAllActiveByUserId(existing.getUser().getId(), Instant.now());
            log.warn("Refresh token reuse detected for userId={}; all sessions revoked", existing.getUser().getId());
            throw new InvalidTokenException("Refresh token reuse detected. Please sign in again");
        }

        if (existing.isExpired()) {
            existing.revoke(null);
            refreshTokenRepository.save(existing);
            throw new ExpiredTokenException("Refresh token has expired");
        }

        final User user = existing.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            revokeAllForUser(user.getId());
            throw new InvalidTokenException("User account is not active");
        }

        if (!user.isEmailVerified()) {
            revokeAllForUser(user.getId());
            throw new EmailNotVerifiedException();
        }

        final User hydratedUser = userRepository.findByIdWithRolesAndPermissions(user.getId())
                .orElseThrow(() -> new InvalidTokenException("User linked to refresh token no longer exists"));

        final String newRawToken = tokenHashingService.generateRawToken();
        final String newTokenHash = tokenHashingService.hash(newRawToken);

        existing.revoke(newTokenHash);
        refreshTokenRepository.save(existing);

        final RefreshToken replacement = new RefreshToken();
        replacement.setUser(hydratedUser);
        replacement.setTenantId(hydratedUser.getTenantId());
        replacement.setTokenHash(newTokenHash);
        replacement.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpiration()));
        replacement.setIpAddress(truncate(ipAddress, 45));
        replacement.setUserAgent(truncate(userAgent, 512));
        refreshTokenRepository.save(replacement);

        return new RotatedTokens(hydratedUser, newRawToken);
    }

    @Override
    public void revokeAllForUser(final UUID userId) {
        final int revoked = refreshTokenRepository.revokeAllActiveByUserId(userId, Instant.now());
        log.info("Revoked {} refresh token(s) for userId={}", revoked, userId);
    }

    @Override
    public void revokeToken(final String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(tokenHashingService.hash(rawRefreshToken.trim()))
                .filter(token -> !token.isRevoked())
                .ifPresent(token -> {
                    token.revoke(null);
                    refreshTokenRepository.save(token);
                });
    }

    private void enforceSessionLimit(final UUID userId) {
        final Instant now = Instant.now();
        final List<RefreshToken> activeTokens =
                refreshTokenRepository.findByUserIdAndRevokedFalseAndExpiresAtAfterOrderByCreatedAtAsc(userId, now);

        int overflow = activeTokens.size() - MAX_ACTIVE_SESSIONS + 1;
        for (int index = 0; overflow > 0 && index < activeTokens.size(); index++, overflow--) {
            final RefreshToken oldest = activeTokens.get(index);
            oldest.revoke(null);
            refreshTokenRepository.save(oldest);
        }
    }

    private static String truncate(final String value, final int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
