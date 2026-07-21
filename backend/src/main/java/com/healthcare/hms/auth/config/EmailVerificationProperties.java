package com.healthcare.hms.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Email verification token lifetime and frontend verification-link base URL.
 */
@Validated
@ConfigurationProperties(prefix = "hms.security.email-verification")
public class EmailVerificationProperties {

    @NotNull
    private Duration tokenExpiration = Duration.ofHours(24);

    @NotBlank
    private String frontendBaseUrl = "http://localhost:3000";

    public Duration getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(final Duration tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(final String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }
}
