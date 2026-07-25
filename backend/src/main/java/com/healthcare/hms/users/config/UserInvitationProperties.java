package com.healthcare.hms.users.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * User invitation token lifetime and frontend accept-link base URL (Phase 4.5).
 */
@Validated
@ConfigurationProperties(prefix = "hms.security.user-invitation")
public class UserInvitationProperties {

    @NotNull
    private Duration tokenExpiration = Duration.ofHours(72);

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
