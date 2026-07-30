package com.healthcare.hms.auth.dto.request;

import com.healthcare.hms.auth.validator.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Step 1 of split hospital onboarding: admin creates their account (no tenant yet).
 */
public record PreRegisterAdminRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @StrongPassword
        String password,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone
) {
}
