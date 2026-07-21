package com.healthcare.hms.auth.service;

import com.healthcare.hms.auth.config.EmailVerificationProperties;
import com.healthcare.hms.common.email.EmailDeliveryException;
import com.healthcare.hms.common.email.EmailMessage;
import com.healthcare.hms.common.email.EmailSender;
import com.healthcare.hms.users.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Builds and sends email-verification messages via {@link EmailSender}.
 */
@Service
public class EmailVerificationEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationEmailService.class);
    private static final String SUBJECT = "Verify your Healthcare HMS email";

    private final EmailSender emailSender;
    private final EmailVerificationProperties emailVerificationProperties;

    public EmailVerificationEmailService(
            final EmailSender emailSender,
            final EmailVerificationProperties emailVerificationProperties
    ) {
        this.emailSender = emailSender;
        this.emailVerificationProperties = emailVerificationProperties;
    }

    public void sendVerificationLink(final User user, final String rawToken) {
        final String verifyUrl = UriComponentsBuilder
                .fromUriString(trimTrailingSlash(emailVerificationProperties.getFrontendBaseUrl()))
                .path("/verify-email")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        final String firstName = user.getFirstName() == null ? "there" : user.getFirstName();
        final String textBody = """
                Hello %s,

                Please verify your Healthcare HMS email address by opening the link below.
                This link expires in %s and can be used once.

                %s

                If you did not create an account, you can ignore this email.
                """.formatted(
                firstName,
                formatDuration(emailVerificationProperties.getTokenExpiration()),
                verifyUrl
        );

        final String htmlBody = """
                <p>Hello %s,</p>
                <p>Please verify your Healthcare HMS email address.</p>
                <p>
                  <a href="%s">Verify email address</a>
                </p>
                <p>This link expires in %s and can be used once.</p>
                <p>If you did not create an account, you can ignore this email.</p>
                """.formatted(
                escapeHtml(firstName),
                escapeHtml(verifyUrl),
                escapeHtml(formatDuration(emailVerificationProperties.getTokenExpiration()))
        );

        try {
            emailSender.send(new EmailMessage(user.getEmail(), SUBJECT, textBody, htmlBody));
        } catch (final EmailDeliveryException exception) {
            log.error("Email verification delivery failed for userId={}", user.getId(), exception);
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
        if (hours >= 1 && duration.toMinutesPart() == 0) {
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
