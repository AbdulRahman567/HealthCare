package com.healthcare.hms.patients.validation;

import com.healthcare.hms.patients.dto.request.EmergencyContactRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates partial emergency-contact payloads are not accepted.
 */
public class EmergencyContactRequestValidator
        implements ConstraintValidator<ValidEmergencyContact, EmergencyContactRequest> {

    @Override
    public boolean isValid(final EmergencyContactRequest value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        final boolean hasName = isPresent(value.name());
        final boolean hasPhone = isPresent(value.phone());
        final boolean hasRelation = isPresent(value.relation());

        if (!hasName && !hasPhone && !hasRelation) {
            return true;
        }

        if (hasName && hasPhone) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        if (!hasName) {
            context.buildConstraintViolationWithTemplate("Emergency contact name is required when contact details are provided")
                    .addPropertyNode("name")
                    .addConstraintViolation();
        }
        if (!hasPhone) {
            context.buildConstraintViolationWithTemplate("Emergency contact phone is required when contact details are provided")
                    .addPropertyNode("phone")
                    .addConstraintViolation();
        }
        return false;
    }

    private static boolean isPresent(final String value) {
        return value != null && !value.isBlank();
    }
}
