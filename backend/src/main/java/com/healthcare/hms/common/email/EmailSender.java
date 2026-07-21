package com.healthcare.hms.common.email;

/**
 * Outbound email delivery abstraction. Implementations may use SMTP or local logging.
 */
public interface EmailSender {

    /**
     * Sends a transactional email message.
     *
     * @param message message payload (recipient, subject, bodies)
     */
    void send(EmailMessage message);
}
