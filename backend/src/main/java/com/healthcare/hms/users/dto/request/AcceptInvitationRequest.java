package com.healthcare.hms.users.dto.request;

import com.healthcare.hms.auth.validator.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Accept invitation and create the invited user account.
 */
public record AcceptInvitationRequest(
        @NotBlank(message = "Invitation token is required")
        @Size(min = 32, max = 256, message = "Invitation token is invalid")
        String token,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Password is required")
        @StrongPassword
        String password,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone
) {
}
