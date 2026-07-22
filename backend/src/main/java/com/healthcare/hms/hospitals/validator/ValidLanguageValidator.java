package com.healthcare.hms.hospitals.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.regex.Pattern;

public class ValidLanguageValidator implements ConstraintValidator<ValidLanguage, String> {

    private static final Pattern LANGUAGE_TAG = Pattern.compile("^[a-zA-Z]{2,3}(-[a-zA-Z0-9]{2,8})*$");

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        final String trimmed = value.trim();
        if (!LANGUAGE_TAG.matcher(trimmed).matches()) {
            return false;
        }
        final Locale locale = Locale.forLanguageTag(trimmed);
        return locale != null && !locale.getLanguage().isBlank();
    }
}
