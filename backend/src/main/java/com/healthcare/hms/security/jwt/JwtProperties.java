package com.healthcare.hms.security.jwt;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Externalized JWT configuration bound from application properties.
 * Rejects short or placeholder secrets so the app cannot start with forgeable keys.
 */
@Validated
@ConfigurationProperties(prefix = "hms.security.jwt")
public class JwtProperties {

    private static final String PLACEHOLDER_MARKER = "change-me";

    @NotBlank
    @Size(min = 32, message = "JWT access token secret must be at least 32 characters")
    private String accessTokenSecret;

    @NotBlank
    @Size(min = 32, message = "JWT refresh token secret must be at least 32 characters")
    private String refreshTokenSecret;

    @NotNull
    private Duration accessTokenExpiration = Duration.ofMinutes(15);

    @NotNull
    private Duration refreshTokenExpiration = Duration.ofDays(7);

    @NotBlank
    private String issuer = "healthcare-hms";

    /**
     * When false (production), placeholder secrets cause startup failure.
     * Local/test profiles may keep true to use documented example values.
     */
    private boolean allowInsecureSecrets = true;

    @PostConstruct
    void validateSecrets() {
        rejectIfPlaceholder("access-token-secret", accessTokenSecret);
        rejectIfPlaceholder("refresh-token-secret", refreshTokenSecret);
    }

    private void rejectIfPlaceholder(final String name, final String secret) {
        if (allowInsecureSecrets) {
            return;
        }
        if (secret != null && secret.toLowerCase(Locale.ROOT).contains(PLACEHOLDER_MARKER)) {
            throw new IllegalStateException(
                    "Refusing to start with insecure JWT " + name
                            + ". Set JWT_SECRET / JWT_REFRESH_SECRET to strong random values "
                            + "(≥32 chars) and set hms.security.jwt.allow-insecure-secrets=false."
            );
        }
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(final String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getRefreshTokenSecret() {
        return refreshTokenSecret;
    }

    public void setRefreshTokenSecret(final String refreshTokenSecret) {
        this.refreshTokenSecret = refreshTokenSecret;
    }

    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(final Duration accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(final Duration refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public boolean isAllowInsecureSecrets() {
        return allowInsecureSecrets;
    }

    public void setAllowInsecureSecrets(final boolean allowInsecureSecrets) {
        this.allowInsecureSecrets = allowInsecureSecrets;
    }
}
