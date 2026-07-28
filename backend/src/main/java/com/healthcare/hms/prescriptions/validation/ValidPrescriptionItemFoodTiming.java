package com.healthcare.hms.prescriptions.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PrescriptionItemFoodTimingValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPrescriptionItemFoodTiming {

    String message() default "Medicine cannot be both before food and after food";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
