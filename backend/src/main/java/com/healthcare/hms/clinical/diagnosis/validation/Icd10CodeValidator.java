package com.healthcare.hms.clinical.diagnosis.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ICD-10-CM format validator (letter + digits, optional decimal extension).
 */
public class Icd10CodeValidator implements ConstraintValidator<ValidIcd10Code, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        return DiagnosisClinicalRules.isValidIcd10Code(value);
    }
}
