package com.healthcare.hms.patients.history.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.time.LocalDate;

/**
 * Reflective date-range check for history request records exposing
 * {@code diagnosisDate()} and {@code recoveryDate()}.
 */
public class ClinicalDateRangeValidator implements ConstraintValidator<ValidClinicalDateRange, Object> {

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            final LocalDate diagnosisDate = (LocalDate) invoke(value, "diagnosisDate");
            final LocalDate recoveryDate = (LocalDate) invoke(value, "recoveryDate");
            if (diagnosisDate == null || recoveryDate == null) {
                return true;
            }
            if (!recoveryDate.isBefore(diagnosisDate)) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Recovery date must be on or after diagnosis date")
                    .addPropertyNode("recoveryDate")
                    .addConstraintViolation();
            return false;
        } catch (final ReflectiveOperationException exception) {
            return true;
        }
    }

    private static Object invoke(final Object target, final String methodName) throws ReflectiveOperationException {
        final Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }
}
