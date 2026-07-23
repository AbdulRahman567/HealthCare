package com.healthcare.hms.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the annotated type or method requires one or more permission codes.
 *
 * <p><strong>Controllers:</strong> enforced by
 * {@link com.healthcare.hms.security.authorization.PermissionAuthorizationInterceptor}
 * before the handler runs — controllers must not contain imperative authorization logic.
 *
 * <p><strong>Services / other beans:</strong> enforced by
 * {@link com.healthcare.hms.security.authorization.PermissionAuthorizationAspect}.
 *
 * <p><strong>Usage — controller (declarative only):</strong>
 * <pre>{@code
 * @GetMapping("/api/v1/patients/{id}")
 * @RequirePermission(PermissionConstants.PATIENT_READ)
 * public ApiResponse<PatientResponse> getPatient(@PathVariable UUID id) {
 *     return ApiResponse.success(patientService.getById(id));
 * }
 * }</pre>
 *
 * <p><strong>Usage — service method:</strong>
 * <pre>{@code
 * @RequirePermission(PermissionConstants.PRESCRIPTION_CREATE)
 * public Prescription create(CreatePrescriptionCommand command) {
 *     // business logic only
 * }
 * }</pre>
 *
 * <p><strong>Usage — require all listed permissions:</strong>
 * <pre>{@code
 * @RequirePermission(
 *     value = { PermissionConstants.PATIENT_UPDATE, PermissionConstants.VISIT_UPDATE },
 *     requireAll = true
 * )
 * }</pre>
 *
 * @see com.healthcare.hms.security.authorization.PermissionGuard
 * @see com.healthcare.hms.users.constant.PermissionConstants
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RequirePermission {

    /**
     * Permission codes from {@link com.healthcare.hms.users.constant.PermissionConstants}.
     * Empty array is treated as authenticated-only (default deny still applies via guard).
     */
    String[] value() default {};

    /**
     * When {@code true}, every listed permission is required; otherwise any one is enough.
     */
    boolean requireAll() default false;
}
