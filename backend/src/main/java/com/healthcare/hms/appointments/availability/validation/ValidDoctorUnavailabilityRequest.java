package com.healthcare.hms.appointments.availability.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidDoctorUnavailabilityRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDoctorUnavailabilityRequest {

    String message() default "Doctor unavailability request is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
