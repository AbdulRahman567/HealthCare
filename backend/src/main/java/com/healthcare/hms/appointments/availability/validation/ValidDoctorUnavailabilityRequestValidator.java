package com.healthcare.hms.appointments.availability.validation;

import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorUnavailabilityRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Unavailability structural rules: date order, timed vs all-day constraints.
 */
public class ValidDoctorUnavailabilityRequestValidator
        implements ConstraintValidator<ValidDoctorUnavailabilityRequest, UpsertDoctorUnavailabilityRequest> {

    @Override
    public boolean isValid(
            final UpsertDoctorUnavailabilityRequest request,
            final ConstraintValidatorContext context
    ) {
        if (request == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;

        if (request.startDate() != null
                && request.endDate() != null
                && request.endDate().isBefore(request.startDate())) {
            context.buildConstraintViolationWithTemplate("endDate must be on or after startDate")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            valid = false;
        }

        if (Boolean.TRUE.equals(request.allDay())) {
            if (request.startTime() != null || request.endTime() != null) {
                context.buildConstraintViolationWithTemplate(
                                "startTime and endTime must be omitted when allDay is true")
                        .addPropertyNode("allDay")
                        .addConstraintViolation();
                valid = false;
            }
            return valid;
        }

        if (request.startDate() != null
                && request.endDate() != null
                && !request.startDate().equals(request.endDate())) {
            context.buildConstraintViolationWithTemplate(
                            "Timed unavailability must be a single day (startDate == endDate)")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            valid = false;
        }
        if (request.startTime() == null || request.endTime() == null) {
            context.buildConstraintViolationWithTemplate(
                            "startTime and endTime are required when allDay is false")
                    .addPropertyNode("startTime")
                    .addConstraintViolation();
            valid = false;
        } else if (!request.endTime().isAfter(request.startTime())) {
            context.buildConstraintViolationWithTemplate("endTime must be after startTime")
                    .addPropertyNode("endTime")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
