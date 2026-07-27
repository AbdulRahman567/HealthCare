package com.healthcare.hms.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Development/fallback sender that logs email metadata without delivering over SMTP.
 * Recipients are masked and bodies are never logged (PHI / secrets stay out of aggregators).
 */
@Component
@ConditionalOnProperty(prefix = "hms.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(final EmailMessage message) {
        log.info(
                "Email (logging sender) to={} subject={} textBodyLength={}",
                EmailLogRedaction.maskRecipient(message.to()),
                message.subject(),
                message.textBody() == null ? 0 : message.textBody().length()
        );
    }
}
