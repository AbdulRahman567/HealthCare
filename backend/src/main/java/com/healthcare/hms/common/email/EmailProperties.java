package com.healthcare.hms.common.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Mail delivery settings. When {@code enabled} is false, emails are logged instead of sent.
 */
@Validated
@ConfigurationProperties(prefix = "hms.mail")
public class EmailProperties {

    private boolean enabled = false;

    @NotBlank
    @Email
    private String from = "noreply@healthcare-hms.local";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }
}
