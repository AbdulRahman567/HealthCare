package com.healthcare.hms.auth.service;

import com.healthcare.hms.auth.config.PasswordResetProperties;
import com.healthcare.hms.common.email.EmailDeliveryException;
import com.healthcare.hms.common.email.EmailMessage;
import com.healthcare.hms.common.email.EmailSender;
import com.healthcare.hms.users.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Builds and sends password-recovery emails via {@link EmailSender}.
 */
@Service
public class PasswordResetEmailService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailService.class);
    private static final String SUBJECT = "Reset your Healthcare HMS password";

    private final EmailSender emailSender;
    private final PasswordResetProperties passwordResetProperties;

    public PasswordResetEmailService(
            final EmailSender emailSender,
            final PasswordResetProperties passwordResetProperties
    ) {
        this.emailSender = emailSender;
        this.passwordResetProperties = passwordResetProperties;
    }

    public void sendResetLink(final User user, final String rawToken) {
        final String resetUrl = UriComponentsBuilder
                .fromUriString(trimTrailingSlash(passwordResetProperties.getFrontendBaseUrl()))
                .path("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        final String firstName = user.getFirstName() == null ? "there" : user.getFirstName();
        final String textBody = """
                Hello %s,

                We received a request to reset your Healthcare HMS password.
                Open the link below to choose a new password. This link expires in %s and can be used once.

                %s

                If you did not request a password reset, you can ignore this email.
                """.formatted(
                firstName,
                formatDuration(passwordResetProperties.getTokenExpiration()),
                resetUrl
        );

        final String htmlBody = """
                <p>Hello %s,</p>
                <p>We received a request to reset your Healthcare HMS password.</p>
                <p>
                  <a href="%s">Reset your password</a>
                </p>
                <p>This link expires in %s and can be used once.</p>
                <p>If you did not request a password reset, you can ignore this email.</p>
                """.formatted(
                escapeHtml(firstName),
                escapeHtml(resetUrl),
                escapeHtml(formatDuration(passwordResetProperties.getTokenExpiration()))
        );

        try {
            emailSender.send(new EmailMessage(user.getEmail(), SUBJECT, textBody, htmlBody));
        } catch (final EmailDeliveryException exception) {
            log.error("Password reset email delivery failed for userId={}", user.getId(), exception);
            throw exception;
        }
    }

    private static String trimTrailingSlash(final String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private static String formatDuration(final java.time.Duration duration) {
        final long hours = duration.toHours();
        if (hours >= 1 && duration.toMinutes() % 60 == 0) {
            return hours == 1 ? "1 hour" : hours + " hours";
        }
        final long minutes = duration.toMinutes();
        return minutes == 1 ? "1 minute" : minutes + " minutes";
    }

    private static String escapeHtml(final String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
