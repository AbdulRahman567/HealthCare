package com.healthcare.hms.hospitals.dto.request;

import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Step 2 of split hospital onboarding: provide the registration token from step 1
 * and the hospital details to complete the tenant + hospital + admin creation.
 */
public record CompleteRegistrationRequest(
        @NotBlank(message = "Registration token is required")
        String registrationToken,

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

        SubscriptionPlan subscriptionPlan
) {
}
