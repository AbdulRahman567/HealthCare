package com.healthcare.hms.appointments.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Structural slot rules shared by create / update / reschedule requests.
 */
public class ValidAppointmentSlotRequestValidator
        implements ConstraintValidator<ValidAppointmentSlotRequest, Object> {

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;

        final LocalTime startTime = invoke(value, "startTime", LocalTime.class);
        final LocalTime endTime = invoke(value, "endTime", LocalTime.class);
        final Integer durationMinutes = invoke(value, "durationMinutes", Integer.class);

        if (startTime != null && endTime != null) {
            if (!endTime.isAfter(startTime)) {
                context.buildConstraintViolationWithTemplate("endTime must be after startTime")
                        .addPropertyNode("endTime")
                        .addConstraintViolation();
                valid = false;
            } else if (durationMinutes != null) {
                final long actual = Duration.between(startTime, endTime).toMinutes();
                if (actual != durationMinutes.longValue()) {
                    context.buildConstraintViolationWithTemplate(
                                    "durationMinutes must equal the difference between startTime and endTime")
                            .addPropertyNode("durationMinutes")
                            .addConstraintViolation();
                    valid = false;
                }
            }
        }

        return valid;
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(final Object target, final String accessor, final Class<T> type) {
        try {
            final Method method = target.getClass().getMethod(accessor);
            final Object result = method.invoke(target);
            if (result == null) {
                return null;
            }
            if (!type.isInstance(result)) {
                return null;
            }
            return (T) result;
        } catch (final ReflectiveOperationException ex) {
            return null;
        }
    }
}
