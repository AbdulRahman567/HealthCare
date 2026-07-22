package com.healthcare.hms.hospitals.dto.request;

import com.healthcare.hms.hospitals.model.WorkingHours;
import com.healthcare.hms.hospitals.validator.ValidCurrency;
import com.healthcare.hms.hospitals.validator.ValidLanguage;
import com.healthcare.hms.hospitals.validator.ValidTimezone;
import com.healthcare.hms.hospitals.validator.ValidWorkingHours;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Full replace payload for the current tenant's hospital settings.
 */
public record UpdateHospitalSettingsRequest(
        @NotBlank(message = "Hospital name is required")
        @Size(min = 2, max = 200, message = "Hospital name must be between 2 and 200 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Size(max = 500, message = "Logo URL must not exceed 500 characters")
        @Pattern(
                regexp = "^$|^https?://.+",
                message = "Logo URL must be an http or https URL"
        )
        String logoUrl,

        @NotBlank(message = "Timezone is required")
        @Size(max = 100, message = "Timezone must not exceed 100 characters")
        @ValidTimezone
        String timezone,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO 4217 code")
        @ValidCurrency
        String currency,

        @NotBlank(message = "Language is required")
        @Size(max = 10, message = "Language must not exceed 10 characters")
        @ValidLanguage
        String language,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Size(max = 30, message = "Secondary phone must not exceed 30 characters")
        String secondaryPhone,

        @Size(max = 500, message = "Website must not exceed 500 characters")
        @Pattern(
                regexp = "^$|^https?://.+",
                message = "Website must be an http or https URL"
        )
        String website,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Size(max = 100, message = "State/province must not exceed 100 characters")
        String stateProvince,

        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        String postalCode,

        @ValidWorkingHours
        WorkingHours workingHours
) {
}
