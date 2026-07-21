package com.healthcare.hms.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.healthcare.hms.auth.config.EmailVerificationProperties;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.common.email.EmailDeliveryException;
import com.healthcare.hms.common.email.EmailMessage;
import com.healthcare.hms.common.email.EmailSender;
import com.healthcare.hms.users.entity.User;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationEmailService")
class EmailVerificationEmailServiceTest {

    @Mock
    private EmailSender emailSender;

    private EmailVerificationEmailService emailVerificationEmailService;
    private User user;

    @BeforeEach
    void setUp() {
        final EmailVerificationProperties emailVerificationProperties = new EmailVerificationProperties();
        emailVerificationProperties.setFrontendBaseUrl("https://app.hms.test");
        emailVerificationProperties.setTokenExpiration(Duration.ofHours(24));
        emailVerificationEmailService =
                new EmailVerificationEmailService(emailSender, emailVerificationProperties);
        user = AuthTestData.activeVerifiedUser("hash");
        user.setEmailVerified(false);
    }

    @Test
    @DisplayName("sends a verification email with the expected recipient and subject")
    void sendVerificationLink_sendsExpectedEmail() {
        emailVerificationEmailService.sendVerificationLink(user, "raw-verify-token");

        final ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());

        final EmailMessage message = captor.getValue();
        assertThat(message.to()).isEqualTo(user.getEmail());
        assertThat(message.subject()).isEqualTo("Verify your Healthcare HMS email");
        assertThat(message.textBody()).contains("raw-verify-token").contains("24 hours");
        assertThat(message.htmlBody()).contains("raw-verify-token").contains("Verify email address");
    }

    @Test
    @DisplayName("embeds the verification link built from the configured frontend base URL")
    void sendVerificationLink_buildsUrlFromFrontendBaseUrl() {
        emailVerificationEmailService.sendVerificationLink(user, "raw-verify-token");

        final ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());

        assertThat(captor.getValue().textBody())
                .contains("https://app.hms.test/verify-email?token=raw-verify-token");
    }

    @Test
    @DisplayName("propagates EmailDeliveryException raised by the sender")
    void sendVerificationLink_rethrowsEmailDeliveryException() {
        final EmailDeliveryException deliveryException =
                new EmailDeliveryException("smtp unavailable", new RuntimeException("connection refused"));
        doThrow(deliveryException).when(emailSender).send(any(EmailMessage.class));

        assertThatThrownBy(() -> emailVerificationEmailService.sendVerificationLink(user, "raw-verify-token"))
                .isSameAs(deliveryException);
    }
}
