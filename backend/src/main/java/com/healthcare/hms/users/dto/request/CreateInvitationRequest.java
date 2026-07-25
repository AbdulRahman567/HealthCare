package com.healthcare.hms.users.dto.request;

import com.healthcare.hms.users.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Hospital admin invite-by-email payload.
 */
public record CreateInvitationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotNull(message = "Role type is required")
        RoleType roleType,

        @Size(max = 500, message = "Message must not exceed 500 characters")
        String message
) {
}
