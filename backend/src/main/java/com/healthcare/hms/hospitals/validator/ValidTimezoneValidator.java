package com.healthcare.hms.hospitals.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.DateTimeException;
import java.time.ZoneId;

public class ValidTimezoneValidator implements ConstraintValidator<ValidTimezone, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            ZoneId.of(value.trim());
            return true;
        } catch (final DateTimeException exception) {
            return false;
        }
    }
}
