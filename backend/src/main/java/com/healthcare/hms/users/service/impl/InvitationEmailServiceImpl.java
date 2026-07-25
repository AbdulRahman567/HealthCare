package com.healthcare.hms.users.service.impl;

import com.healthcare.hms.common.email.EmailDeliveryException;
import com.healthcare.hms.common.email.EmailMessage;
import com.healthcare.hms.common.email.EmailSender;
import com.healthcare.hms.users.config.UserInvitationProperties;
import com.healthcare.hms.users.entity.UserInvitation;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.service.InvitationEmailService;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Builds invitation emails and delegates delivery to {@link EmailSender}.
 */
@Service
public class InvitationEmailServiceImpl implements InvitationEmailService {

    private static final Logger log = LoggerFactory.getLogger(InvitationEmailServiceImpl.class);

    private final EmailSender emailSender;
    private final UserInvitationProperties invitationProperties;

    public InvitationEmailServiceImpl(
            final EmailSender emailSender,
            final UserInvitationProperties invitationProperties
    ) {
        this.emailSender = emailSender;
        this.invitationProperties = invitationProperties;
    }

    @Override
    public void sendInvitation(
            final UserInvitation invitation,
            final String rawToken,
            final String hospitalName
    ) {
        send(invitation, rawToken, hospitalName, "You're invited to join " + hospitalName, false);
    }

    @Override
    public void sendInvitationResent(
            final UserInvitation invitation,
            final String rawToken,
            final String hospitalName
    ) {
        send(invitation, rawToken, hospitalName, "Invitation reminder — join " + hospitalName, true);
    }

    private void send(
            final UserInvitation invitation,
            final String rawToken,
            final String hospitalName,
            final String subject,
            final boolean resent
    ) {
        final String acceptUrl = UriComponentsBuilder
                .fromUriString(trimTrailingSlash(invitationProperties.getFrontendBaseUrl()))
                .path("/accept-invitation")
                .fragment("token=" + rawToken)
                .build()
                .toUriString();

        final String greetingName = invitation.getFirstName() == null ? "there" : invitation.getFirstName();
        final String roleLabel = formatRole(invitation.getRoleType());
        final String expiry = formatDuration(invitationProperties.getTokenExpiration());
        final String intro = resent
                ? "This is a reminder that you have been invited to join " + hospitalName + "."
                : "You have been invited to join " + hospitalName + " on Healthcare HMS.";

        final String textBody = """
                Hello %s,

                %s
                Role: %s

                Accept your invitation (expires in %s, single use):
                %s

                If you did not expect this invitation, you can ignore this email or reject it from the link page.
                """.formatted(greetingName, intro, roleLabel, expiry, acceptUrl);

        final String htmlBody = """
                <p>Hello %s,</p>
                <p>%s</p>
                <p><strong>Role:</strong> %s</p>
                <p><a href="%s">Accept invitation</a></p>
                <p>This link expires in %s and can be used once.</p>
                <p>If you did not expect this invitation, you can ignore this email.</p>
                """.formatted(
                escapeHtml(greetingName),
                escapeHtml(intro),
                escapeHtml(roleLabel),
                escapeHtml(acceptUrl),
                escapeHtml(expiry)
        );

        try {
            emailSender.send(new EmailMessage(invitation.getEmail(), subject, textBody, htmlBody));
        } catch (final EmailDeliveryException exception) {
            log.error("Invitation email delivery failed invitationId={}", invitation.getId(), exception);
            throw exception;
        }
    }

    private static String formatRole(final RoleType roleType) {
        return roleType.name().replace('_', ' ');
    }

    private static String trimTrailingSlash(final String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private static String formatDuration(final Duration duration) {
        final long hours = duration.toHours();
        if (hours >= 24 && duration.toMinutesPart() == 0 && duration.toSecondsPart() == 0) {
            final long days = hours / 24;
            return days == 1 ? "1 day" : days + " days";
        }
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
