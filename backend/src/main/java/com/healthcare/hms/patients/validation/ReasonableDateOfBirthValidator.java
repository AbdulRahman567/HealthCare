package com.healthcare.hms.patients.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

/**
 * Ensures DOB is not older than the configured maximum age.
 */
public class ReasonableDateOfBirthValidator implements ConstraintValidator<ReasonableDateOfBirth, LocalDate> {

    private int maxAgeYears;

    @Override
    public void initialize(final ReasonableDateOfBirth annotation) {
        this.maxAgeYears = annotation.maxAgeYears();
    }

    @Override
    public boolean isValid(final LocalDate value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        final LocalDate earliest = LocalDate.now().minusYears(maxAgeYears);
        return !value.isBefore(earliest);
    }
}
