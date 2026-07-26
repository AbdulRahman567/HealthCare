package com.healthcare.hms.patients.dto.request;

import com.healthcare.hms.patients.validation.ValidEmergencyContact;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Primary next-of-kin / emergency contact on registration.
 *
 * <p>When any field is provided, name and phone are both required
 * (enforced by {@link ValidEmergencyContact}).
 */
@ValidEmergencyContact
public record EmergencyContactRequest(
        @Size(max = 150, message = "Emergency contact name must not exceed 150 characters")
        String name,

        @Size(max = 30, message = "Emergency contact phone must not exceed 30 characters")
        @Pattern(
                regexp = PatientContactPatterns.PHONE_OPTIONAL,
                message = "Emergency contact phone format is invalid"
        )
        String phone,

        @Size(max = 50, message = "Emergency contact relation must not exceed 50 characters")
        String relation
) {
}
