package com.healthcare.hms.common.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * SMTP-backed email delivery using Spring's {@link JavaMailSender}.
 */
@Component
@ConditionalOnProperty(prefix = "hms.mail", name = "enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;

    public SmtpEmailSender(final JavaMailSender javaMailSender, final EmailProperties emailProperties) {
        this.javaMailSender = javaMailSender;
        this.emailProperties = emailProperties;
    }

    @Override
    public void send(final EmailMessage message) {
        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(emailProperties.getFrom());
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.textBody(), message.htmlBody());
            javaMailSender.send(mimeMessage);
            log.info("Email sent to={}", message.to());
        } catch (final MessagingException | MailException exception) {
            log.error("Failed to send email to={}", message.to(), exception);
            throw new EmailDeliveryException("Failed to deliver email", exception);
        }
    }
}
