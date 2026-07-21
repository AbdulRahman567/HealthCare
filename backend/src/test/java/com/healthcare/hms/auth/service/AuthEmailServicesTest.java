package com.healthcare.hms.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.auth.config.EmailVerificationProperties;
import com.healthcare.hms.auth.config.PasswordResetProperties;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.common.email.EmailDeliveryException;
import com.healthcare.hms.common.email.EmailMessage;
import com.healthcare.hms.common.email.EmailSender;
import com.healthcare.hms.users.entity.User;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth email services")
class AuthEmailServicesTest {

    @Mock
    private EmailSender emailSender;
    @Mock
    private PasswordResetProperties passwordResetProperties;
    @Mock
    private EmailVerificationProperties emailVerificationProperties;

    @Test
    @DisplayName("password reset email is sent with reset link")
    void sendResetLink() {
        when(passwordResetProperties.getFrontendBaseUrl()).thenReturn("http://localhost:3000");
        when(passwordResetProperties.getTokenExpiration()).thenReturn(Duration.ofHours(1));
        final User user = AuthTestData.activeVerifiedUser("hash");

        new PasswordResetEmailService(emailSender, passwordResetProperties)
                .sendResetLink(user, "raw-token");

        final ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());
        assertThat(captor.getValue().to()).isEqualTo(user.getEmail());
        assertThat(captor.getValue().subject()).contains("Reset");
        assertThat(captor.getValue().textBody()).contains("raw-token");
    }

    @Test
    @DisplayName("verification email is sent with verify link")
    void sendVerificationLink() {
        when(emailVerificationProperties.getFrontendBaseUrl()).thenReturn("http://localhost:3000");
        when(emailVerificationProperties.getTokenExpiration()).thenReturn(Duration.ofHours(24));
        final User user = AuthTestData.activeVerifiedUser("hash");
        user.setEmailVerified(false);

        new EmailVerificationEmailService(emailSender, emailVerificationProperties)
                .sendVerificationLink(user, "raw-token");

        final ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());
        assertThat(captor.getValue().to()).isEqualTo(user.getEmail());
        assertThat(captor.getValue().subject()).contains("Verify");
        assertThat(captor.getValue().textBody()).contains("raw-token");
    }

    @Test
    @DisplayName("delivery failures are rethrown")
    void deliveryFailure() {
        when(passwordResetProperties.getFrontendBaseUrl()).thenReturn("http://localhost:3000");
        when(passwordResetProperties.getTokenExpiration()).thenReturn(Duration.ofHours(1));
        doThrow(new EmailDeliveryException("fail", new RuntimeException("smtp")))
                .when(emailSender).send(any());

        assertThatThrownBy(() -> new PasswordResetEmailService(emailSender, passwordResetProperties)
                .sendResetLink(AuthTestData.activeVerifiedUser("hash"), "raw"))
                .isInstanceOf(EmailDeliveryException.class);
    }
}
