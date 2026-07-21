package com.healthcare.hms.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.healthcare.hms.auth.config.PasswordResetProperties;
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
@DisplayName("PasswordResetEmailService")
class PasswordResetEmailServiceTest {

    @Mock
    private EmailSender emailSender;

    private PasswordResetEmailService passwordResetEmailService;
    private User user;

    @BeforeEach
    void setUp() {
        final PasswordResetProperties passwordResetProperties = new PasswordResetProperties();
        passwordResetProperties.setFrontendBaseUrl("https://app.hms.test");
        passwordResetProperties.setTokenExpiration(Duration.ofHours(1));
        passwordResetEmailService = new PasswordResetEmailService(emailSender, passwordResetProperties);
        user = AuthTestData.activeVerifiedUser("hash");
    }

    @Test
    @DisplayName("sends a reset-link email with the expected recipient and subject")
    void sendResetLink_sendsExpectedEmail() {
        passwordResetEmailService.sendResetLink(user, "raw-reset-token");

        final ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());

        final EmailMessage message = captor.getValue();
        assertThat(message.to()).isEqualTo(user.getEmail());
        assertThat(message.subject()).isEqualTo("Reset your Healthcare HMS password");
        assertThat(message.textBody()).contains("raw-reset-token").contains("1 hour");
        assertThat(message.htmlBody()).contains("raw-reset-token").contains("Reset your password");
    }

    @Test
    @DisplayName("embeds the reset link built from the configured frontend base URL")
    void sendResetLink_buildsUrlFromFrontendBaseUrl() {
        passwordResetEmailService.sendResetLink(user, "raw-reset-token");

        final ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());

        assertThat(captor.getValue().textBody())
                .contains("https://app.hms.test/reset-password?token=raw-reset-token");
    }

    @Test
    @DisplayName("propagates EmailDeliveryException raised by the sender")
    void sendResetLink_rethrowsEmailDeliveryException() {
        final EmailDeliveryException deliveryException =
                new EmailDeliveryException("smtp unavailable", new RuntimeException("connection refused"));
        doThrow(deliveryException).when(emailSender).send(any(EmailMessage.class));

        assertThatThrownBy(() -> passwordResetEmailService.sendResetLink(user, "raw-reset-token"))
                .isSameAs(deliveryException);
    }
}
