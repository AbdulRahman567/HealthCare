package com.healthcare.hms.patients.immunization.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.time.LocalDate;

/**
 * Reflective date-range check for immunization request records exposing
 * {@code administrationDate()} and {@code nextDueDate()}.
 */
public class ImmunizationDateRangeValidator implements ConstraintValidator<ValidImmunizationDateRange, Object> {

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            final LocalDate administrationDate = (LocalDate) invoke(value, "administrationDate");
            final LocalDate nextDueDate = (LocalDate) invoke(value, "nextDueDate");
            if (administrationDate == null || nextDueDate == null) {
                return true;
            }
            if (!nextDueDate.isBefore(administrationDate)) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Next due date must be on or after administration date")
                    .addPropertyNode("nextDueDate")
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
