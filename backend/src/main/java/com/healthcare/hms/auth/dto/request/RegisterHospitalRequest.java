package com.healthcare.hms.auth.dto.request;

import com.healthcare.hms.hospitals.enums.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for registering a new hospital tenant.
 */
public record RegisterHospitalRequest(
        @NotBlank(message = "Hospital name is required")
        @Size(min = 2, max = 200, message = "Hospital name must be between 2 and 200 characters")
        String hospitalName,

        @NotBlank(message = "Hospital email is required")
        @Email(message = "Hospital email must be valid")
        @Size(max = 255, message = "Hospital email must not exceed 255 characters")
        String email,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        SubscriptionPlan subscriptionPlan
) {
}
