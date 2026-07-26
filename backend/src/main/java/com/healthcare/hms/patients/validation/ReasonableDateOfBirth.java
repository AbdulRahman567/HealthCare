package com.healthcare.hms.patients.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rejects dates of birth that are unrealistically far in the past for registration.
 */
@Documented
@Constraint(validatedBy = ReasonableDateOfBirthValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReasonableDateOfBirth {

    String message() default "Date of birth must be within the last 150 years";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int maxAgeYears() default 150;
}
