package com.healthcare.hms.hospitals.dto.request;

import com.healthcare.hms.auth.validator.StrongPassword;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Atomic hospital onboarding payload: tenant + default hospital + initial administrator.
 */
public record HospitalRegistrationRequest(
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

        SubscriptionPlan subscriptionPlan,

        @NotBlank(message = "Administrator first name is required")
        @Size(max = 100, message = "Administrator first name must not exceed 100 characters")
        String adminFirstName,

        @NotBlank(message = "Administrator last name is required")
        @Size(max = 100, message = "Administrator last name must not exceed 100 characters")
        String adminLastName,

        @NotBlank(message = "Administrator email is required")
        @Email(message = "Administrator email must be valid")
        @Size(max = 255, message = "Administrator email must not exceed 255 characters")
        String adminEmail,

        @NotBlank(message = "Administrator password is required")
        @StrongPassword
        String adminPassword,

        @Size(max = 30, message = "Administrator phone must not exceed 30 characters")
        String adminPhone
) {
}
