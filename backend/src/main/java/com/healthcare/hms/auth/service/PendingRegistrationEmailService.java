package com.healthcare.hms.auth.service;

import com.healthcare.hms.auth.config.EmailVerificationProperties;
import com.healthcare.hms.common.email.EmailDeliveryException;
import com.healthcare.hms.common.email.EmailMessage;
import com.healthcare.hms.common.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Builds and sends the registration verification email. Unlike
 * {@link EmailVerificationEmailService}, the recipient has NO account yet — only a
 * pending registration exists, so the link carries a pending-registration token that,
 * when clicked, creates the real tenant/hospital/admin records (Phase 7).
 */
@Service
public class PendingRegistrationEmailService {

    private static final Logger log = LoggerFactory.getLogger(PendingRegistrationEmailService.class);
    private static final String SUBJECT = "Verify your Healthcare HMS registration";

    private final EmailSender emailSender;
    private final EmailVerificationProperties emailVerificationProperties;

    public PendingRegistrationEmailService(
            final EmailSender emailSender,
            final EmailVerificationProperties emailVerificationProperties
    ) {
        this.emailSender = emailSender;
        this.emailVerificationProperties = emailVerificationProperties;
    }

    public void sendVerificationLink(final String email, final String firstName, final String rawToken) {
        final String verifyUrl = UriComponentsBuilder
                .fromUriString(trimTrailingSlash(emailVerificationProperties.getFrontendBaseUrl()))
                .path("/verify-registration")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        final String givenName = firstName == null || firstName.isBlank() ? "there" : firstName;
        final String textBody = """
                Hello %s,

                You've started a Healthcare HMS hospital registration. To create your hospital
                account, open the link below. It expires in %s and can be used once.

                %s

                No hospital account is created until you verify. If you did not start a
                registration, you can ignore this email.
                """.formatted(
                givenName,
                formatDuration(emailVerificationProperties.getTokenExpiration()),
                verifyUrl
        );

        final String htmlBody = """
                <p>Hello %s,</p>
                <p>You've started a Healthcare HMS hospital registration. To create your hospital account, click the button below.</p>
                <p>
                  <a href="%s">Verify and create your account</a>
                </p>
                <p>This link expires in %s and can be used once.</p>
                <p>No hospital account is created until you verify. If you did not start a registration, you can ignore this email.</p>
                """.formatted(
                escapeHtml(givenName),
                escapeHtml(verifyUrl),
                escapeHtml(formatDuration(emailVerificationProperties.getTokenExpiration()))
        );

        try {
            emailSender.send(new EmailMessage(email, SUBJECT, textBody, htmlBody));
        } catch (final EmailDeliveryException exception) {
            log.error("Registration verification email delivery failed for email={}", email, exception);
            throw exception;
        }
    }

    private static String trimTrailingSlash(final String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
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
