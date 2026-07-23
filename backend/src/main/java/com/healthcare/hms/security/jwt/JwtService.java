package com.healthcare.hms.security.jwt;

import com.healthcare.hms.common.exception.auth.AuthenticationException;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.common.exception.auth.TokenValidationException;
import com.healthcare.hms.security.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * JWT infrastructure service for creating and validating access and refresh tokens.
 * Does not perform login or credential authentication.
 */
@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;

    public JwtService(final JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.accessTokenKey = Keys.hmacShaKeyFor(
                jwtProperties.getAccessTokenSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshTokenKey = Keys.hmacShaKeyFor(
                jwtProperties.getRefreshTokenSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(final JwtClaims claims) {
        return generateToken(claims, JwtTokenType.ACCESS);
    }

    public String generateRefreshToken(final JwtClaims claims) {
        return generateToken(claims, JwtTokenType.REFRESH);
    }

    public JwtClaims parseAccessToken(final String token) {
        return parseToken(token, JwtTokenType.ACCESS);
    }

    public JwtClaims parseRefreshToken(final String token) {
        return parseToken(token, JwtTokenType.REFRESH);
    }

    public boolean isAccessTokenValid(final String token) {
        try {
            parseAccessToken(token);
            return true;
        } catch (final AuthenticationException ignored) {
            return false;
        }
    }

    private String generateToken(final JwtClaims claims, final JwtTokenType tokenType) {
        final Instant issuedAt = Instant.now();
        final Instant expiresAt = issuedAt.plus(
                tokenType == JwtTokenType.ACCESS
                        ? jwtProperties.getAccessTokenExpiration()
                        : jwtProperties.getRefreshTokenExpiration());

        final var builder = Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(claims.userId().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, tokenType.name())
                .claim(SecurityConstants.CLAIM_USER_ID, claims.userId().toString())
                .claim(SecurityConstants.CLAIM_EMAIL, claims.email())
                .claim(SecurityConstants.CLAIM_ROLES, List.copyOf(claims.roles()))
                .claim(SecurityConstants.CLAIM_PERMISSIONS, List.copyOf(claims.permissions()))
                .claim(SecurityConstants.CLAIM_TOKEN_VERSION, claims.tokenVersion());

        // Platform Super Admin may omit tenant; hospital principals always carry one.
        if (claims.tenantId() != null) {
            builder.claim(SecurityConstants.CLAIM_TENANT_ID, claims.tenantId().toString());
        }

        return builder.signWith(resolveKey(tokenType)).compact();
    }

    private JwtClaims parseToken(final String token, final JwtTokenType expectedType) {
        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(resolveKey(expectedType))
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            final JwtTokenType actualType = JwtTokenType.valueOf(
                    requireClaim(claims, SecurityConstants.CLAIM_TOKEN_TYPE, String.class));

            if (actualType != expectedType) {
                throw new TokenValidationException(
                        "Unexpected token type. Expected " + expectedType + " but received " + actualType);
            }

            return new JwtClaims(
                    UUID.fromString(requireClaim(claims, SecurityConstants.CLAIM_USER_ID, String.class)),
                    optionalUuidClaim(claims, SecurityConstants.CLAIM_TENANT_ID),
                    requireClaim(claims, SecurityConstants.CLAIM_EMAIL, String.class),
                    toStringSet(claims.get(SecurityConstants.CLAIM_ROLES, Collection.class)),
                    toStringSet(claims.get(SecurityConstants.CLAIM_PERMISSIONS, Collection.class)),
                    requireClaim(claims, SecurityConstants.CLAIM_TOKEN_VERSION, Number.class).longValue(),
                    actualType
            );
        } catch (final ExpiredJwtException exception) {
            throw new ExpiredTokenException("Token has expired", exception);
        } catch (final TokenValidationException exception) {
            throw exception;
        } catch (final JwtException | IllegalArgumentException exception) {
            throw new InvalidTokenException("Token is invalid", exception);
        }
    }

    private SecretKey resolveKey(final JwtTokenType tokenType) {
        return tokenType == JwtTokenType.ACCESS ? accessTokenKey : refreshTokenKey;
    }

    private static <T> T requireClaim(final Claims claims, final String claimName, final Class<T> type) {
        final T value = claims.get(claimName, type);
        if (value == null) {
            throw new TokenValidationException("Missing required claim: " + claimName);
        }
        return value;
    }

    private static UUID optionalUuidClaim(final Claims claims, final String claimName) {
        final String raw = claims.get(claimName, String.class);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return UUID.fromString(raw);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> toStringSet(final Collection<?> values) {
        if (values == null) {
            return Set.of();
        }
        final Set<String> result = new HashSet<>();
        for (final Object value : values) {
            if (value != null) {
                result.add(String.valueOf(value));
            }
        }
        return Set.copyOf(result);
    }
}
