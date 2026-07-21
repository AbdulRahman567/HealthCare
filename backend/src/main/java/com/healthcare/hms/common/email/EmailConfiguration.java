package com.healthcare.hms.common.email;

import com.healthcare.hms.auth.config.PasswordResetProperties;
import com.healthcare.hms.auth.config.EmailVerificationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds mail, password-reset, and email-verification configuration properties.
 */
@Configuration
@EnableConfigurationProperties({
        EmailProperties.class,
        PasswordResetProperties.class,
        EmailVerificationProperties.class
})
public class EmailConfiguration {
}
