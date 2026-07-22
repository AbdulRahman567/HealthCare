package com.healthcare.hms.hospitals.validator;

import com.healthcare.hms.hospitals.model.WorkingDayHours;
import com.healthcare.hms.hospitals.model.WorkingHours;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidWorkingHoursValidator implements ConstraintValidator<ValidWorkingHours, WorkingHours> {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public boolean isValid(final WorkingHours value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;
        valid &= validateDay("monday", value.getMonday(), context);
        valid &= validateDay("tuesday", value.getTuesday(), context);
        valid &= validateDay("wednesday", value.getWednesday(), context);
        valid &= validateDay("thursday", value.getThursday(), context);
        valid &= validateDay("friday", value.getFriday(), context);
        valid &= validateDay("saturday", value.getSaturday(), context);
        valid &= validateDay("sunday", value.getSunday(), context);
        return valid;
    }

    private boolean validateDay(
            final String dayName,
            final WorkingDayHours day,
            final ConstraintValidatorContext context
    ) {
        if (day == null) {
            return true;
        }
        if (day.isClosed()) {
            return true;
        }

        final String open = day.getOpen();
        final String close = day.getClose();
        if (open == null || open.isBlank() || close == null || close.isBlank()) {
            addViolation(context, dayName, "Open and close times are required when the day is not closed");
            return false;
        }

        final LocalTime openTime = parseTime(open);
        final LocalTime closeTime = parseTime(close);
        if (openTime == null || closeTime == null) {
            addViolation(context, dayName, "Open and close times must use HH:mm format");
            return false;
        }
        if (!openTime.isBefore(closeTime)) {
            addViolation(context, dayName, "Open time must be before close time");
            return false;
        }
        return true;
    }

    private static LocalTime parseTime(final String value) {
        try {
            return LocalTime.parse(value.trim(), TIME_FORMAT);
        } catch (final DateTimeParseException exception) {
            return null;
        }
    }

    private static void addViolation(
            final ConstraintValidatorContext context,
            final String dayName,
            final String message
    ) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(dayName)
                .addConstraintViolation();
    }
}
