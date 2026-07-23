package com.healthcare.hms.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the annotated type or method requires a valid authenticated principal
 * (JWT verified, roles/permissions loaded) but no specific permission code.
 *
 * <p>Use for self-service endpoints such as profile, logout, and change-password.
 * Prefer {@link RequirePermission} when a catalog permission applies.
 *
 * <p>Enforced by
 * {@link com.healthcare.hms.security.authorization.PermissionAuthorizationInterceptor}
 * (controllers) and
 * {@link com.healthcare.hms.security.authorization.PermissionAuthorizationAspect}
 * (services).
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RequireAuthenticated {
}
