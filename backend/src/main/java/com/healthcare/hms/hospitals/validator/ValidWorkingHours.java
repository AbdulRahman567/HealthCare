package com.healthcare.hms.hospitals.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates weekly working-hours structure (HH:mm open/close, open before close when open).
 */
@Documented
@Constraint(validatedBy = ValidWorkingHoursValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidWorkingHours {

    String message() default "Working hours are invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
