package com.healthcare.hms.patients.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures emergency contact is either fully absent or has both name and phone.
 */
@Documented
@Constraint(validatedBy = EmergencyContactRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmergencyContact {

    String message() default "Emergency contact requires both name and phone when provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
