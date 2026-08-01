package com.healthcare.hms.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Development/fallback sender that logs email metadata without delivering over SMTP.
 * Recipients are masked and bodies are never logged by default (PHI / secrets stay out
 * of aggregators).
 *
 * <p>Set {@code hms.mail.log-link=true} to also log the full text body (used in local/dev
 * to surface verification/reset links). Off by default; never enabled in production.
 */
@Component
@ConditionalOnProperty(prefix = "hms.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    private final boolean logLink;

    public LoggingEmailSender(@Value("${hms.mail.log-link:false}") final boolean logLink) {
        this.logLink = logLink;
    }

    @Override
    public void send(final EmailMessage message) {
        log.info(
                "Email (logging sender) to={} subject={} textBodyLength={}",
                EmailLogRedaction.maskRecipient(message.to()),
                message.subject(),
                message.textBody() == null ? 0 : message.textBody().length()
        );
        if (logLink && message.textBody() != null) {
            log.info("Email (logging sender) textBody=\n{}", message.textBody());
        }
    }
}
