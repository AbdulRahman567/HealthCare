package com.healthcare.hms.common.email;

/**
 * Immutable email payload for the {@link EmailSender}.
 */
public record EmailMessage(
        String to,
        String subject,
        String textBody,
        String htmlBody
) {
}
