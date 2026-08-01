package com.healthcare.hms.auth.dto.request;

import com.healthcare.hms.auth.validator.StrongPassword;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Single-page hospital registration payload (Phase 7): admin account + hospital details
 * + chosen plan. Only a lightweight pending record is created from this; no real
 * tenant/hospital/admin account exists until the emailed verification link is clicked.
 */
public record RegistrationRequest(
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
        String phone,

        @NotBlank(message = "Hospital name is required")
        @Size(min = 2, max = 200, message = "Hospital name must be between 2 and 200 characters")
        String hospitalName,

        @NotBlank(message = "Hospital email is required")
        @Email(message = "Hospital email must be valid")
        @Size(max = 255, message = "Hospital email must not exceed 255 characters")
        String hospitalEmail,

        @Size(max = 30, message = "Hospital phone must not exceed 30 characters")
        String hospitalPhone,

        @Size(max = 500, message = "Hospital address must not exceed 500 characters")
        String hospitalAddress,

        @NotNull(message = "Subscription plan is required")
        SubscriptionPlan subscriptionPlan
) {
}
