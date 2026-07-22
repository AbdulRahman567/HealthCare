package com.healthcare.hms.hospitals.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Currency;
import java.util.Locale;

public class ValidCurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            Currency.getInstance(value.trim().toUpperCase(Locale.ROOT));
            return true;
        } catch (final IllegalArgumentException exception) {
            return false;
        }
    }
}
