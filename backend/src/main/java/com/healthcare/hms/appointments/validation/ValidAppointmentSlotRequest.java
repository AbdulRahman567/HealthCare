package com.healthcare.hms.appointments.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidAppointmentSlotRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAppointmentSlotRequest {

    String message() default "Appointment slot is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
