package com.healthcare.hms.patients.dto.response;

/**
 * Emergency contact view returned with a patient profile.
 */
public record EmergencyContactResponse(
        String name,
        String phone,
        String relation
) {
}
