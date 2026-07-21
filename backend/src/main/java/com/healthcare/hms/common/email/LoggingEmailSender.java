package com.healthcare.hms.common.email;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Development/fallback sender that logs email metadata without delivering over SMTP.
 * Token query parameters are redacted so reset/verification secrets never enter log aggregators.
 */
@Component
@ConditionalOnProperty(prefix = "hms.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    private static final Pattern TOKEN_QUERY_PARAM = Pattern.compile(
            "([?&](?:token|code)=)[^&\\s]+",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void send(final EmailMessage message) {
        log.info(
                "Email (logging sender) to={} subject={} textBody={}",
                message.to(),
                message.subject(),
                redactSecrets(message.textBody())
        );
    }

    static String redactSecrets(final String body) {
        if (body == null || body.isBlank()) {
            return body;
        }
        return TOKEN_QUERY_PARAM.matcher(body).replaceAll("$1[REDACTED]");
    }
}
