package com.healthcare.hms.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.common.exception.auth.TokenValidationException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private JwtClaims accessClaims;

    @BeforeEach
    void setUp() {
        final JwtProperties properties = new JwtProperties();
        properties.setAccessTokenSecret("test-access-token-secret-key-min-32-chars!!");
        properties.setRefreshTokenSecret("test-refresh-token-secret-key-min-32-chars!");
        properties.setAccessTokenExpiration(Duration.ofMinutes(15));
        properties.setRefreshTokenExpiration(Duration.ofDays(7));
        properties.setIssuer("healthcare-hms-test");
        jwtService = new JwtService(properties);

        accessClaims = new JwtClaims(
                UUID.fromString("22222222-2222-4222-8222-222222222222"),
                UUID.fromString("11111111-1111-4111-8111-111111111111"),
                "admin@hospital.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("HOSPITAL_READ"),
                0L,
                JwtTokenType.ACCESS
        );
    }

    @Test
    @DisplayName("access token round-trip preserves claims")
    void accessTokenRoundTrip() {
        final String token = jwtService.generateAccessToken(accessClaims);
        final JwtClaims parsed = jwtService.parseAccessToken(token);

        assertThat(parsed.userId()).isEqualTo(accessClaims.userId());
        assertThat(parsed.tenantId()).isEqualTo(accessClaims.tenantId());
        assertThat(parsed.email()).isEqualTo(accessClaims.email());
        assertThat(parsed.roles()).containsExactlyInAnyOrderElementsOf(accessClaims.roles());
        assertThat(parsed.permissions()).containsExactlyInAnyOrderElementsOf(accessClaims.permissions());
        assertThat(parsed.tokenVersion()).isEqualTo(0L);
        assertThat(parsed.tokenType()).isEqualTo(JwtTokenType.ACCESS);
    }

    @Test
    @DisplayName("refresh token round-trip works")
    void refreshTokenRoundTrip() {
        final JwtClaims refreshClaims = new JwtClaims(
                accessClaims.userId(),
                accessClaims.tenantId(),
                accessClaims.email(),
                accessClaims.roles(),
                accessClaims.permissions(),
                0L,
                JwtTokenType.REFRESH
        );

        final String token = jwtService.generateRefreshToken(refreshClaims);
        final JwtClaims parsed = jwtService.parseRefreshToken(token);

        assertThat(parsed.tokenType()).isEqualTo(JwtTokenType.REFRESH);
        assertThat(parsed.userId()).isEqualTo(refreshClaims.userId());
    }

    @Test
    @DisplayName("parsing access token as refresh token fails")
    void wrongTokenTypeFails() {
        final String accessToken = jwtService.generateAccessToken(accessClaims);

        assertThatThrownBy(() -> jwtService.parseRefreshToken(accessToken))
                .isInstanceOfAny(TokenValidationException.class, InvalidTokenException.class);
    }

    @Test
    @DisplayName("isAccessTokenValid returns true for valid token")
    void isAccessTokenValid_true() {
        final String token = jwtService.generateAccessToken(accessClaims);
        assertThat(jwtService.isAccessTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isAccessTokenValid returns false for garbage")
    void isAccessTokenValid_false() {
        assertThat(jwtService.isAccessTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    @DisplayName("platform Super Admin tokens may omit tenant claim")
    void platformSuperAdminTokenRoundTripWithoutTenant() {
        final JwtClaims platformClaims = new JwtClaims(
                UUID.fromString("33333333-3333-4333-8333-333333333333"),
                null,
                "super@platform.test",
                Set.of("SUPER_ADMIN"),
                Set.of("HOSPITAL_READ", "DASHBOARD_READ"),
                0L,
                JwtTokenType.ACCESS
        );

        final String token = jwtService.generateAccessToken(platformClaims);
        final JwtClaims parsed = jwtService.parseAccessToken(token);

        assertThat(parsed.tenantId()).isNull();
        assertThat(parsed.userId()).isEqualTo(platformClaims.userId());
        assertThat(parsed.roles()).containsExactly("SUPER_ADMIN");
    }
}
