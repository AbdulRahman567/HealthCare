package com.healthcare.hms.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires the authenticated principal to hold at least one of the listed role types.
 * Enforced by {@link com.healthcare.hms.security.authorization.PermissionAuthorizationInterceptor}.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    /**
     * Role type names matching {@link com.healthcare.hms.users.enums.RoleType} (e.g. HOSPITAL_ADMIN).
     */
    String[] value();

    /**
     * When true, every listed role is required; otherwise any one is sufficient.
     */
    boolean requireAll() default false;
}
