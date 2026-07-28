package com.healthcare.hms.prescriptions.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PrescriptionItemsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPrescriptionItems {

    String message() default "Prescription must not contain duplicate medicines";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
