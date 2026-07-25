package com.healthcare.hms.common.email;

import com.healthcare.hms.auth.config.PasswordResetProperties;
import com.healthcare.hms.auth.config.EmailVerificationProperties;
import com.healthcare.hms.users.config.UserInvitationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds mail, password-reset, and email-verification configuration properties.
 */
@Configuration
@EnableConfigurationProperties({
        EmailProperties.class,
        PasswordResetProperties.class,
        EmailVerificationProperties.class,
        UserInvitationProperties.class
})
public class EmailConfiguration {
}
