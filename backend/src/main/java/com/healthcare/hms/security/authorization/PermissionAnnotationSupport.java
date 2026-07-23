package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.annotation.PublicEndpoint;
import com.healthcare.hms.security.annotation.RequireAuthenticated;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.annotation.RequiresPermission;
import com.healthcare.hms.security.annotation.RequiresRole;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Resolves permission/role/authentication authorization annotations from methods and types.
 *
 * <p>Method-level annotations take precedence over type-level annotations.
 * Supports both {@link RequirePermission} and legacy {@link RequiresPermission}
 * (composed via {@code @AliasFor} so they cannot drift).
 */
public final class PermissionAnnotationSupport {

    private PermissionAnnotationSupport() {
    }

    public static Optional<PermissionRequirement> findPermissionRequirement(
            final Method method,
            final Class<?> targetClass
    ) {
        final RequirePermission methodPermission =
                AnnotatedElementUtils.findMergedAnnotation(method, RequirePermission.class);
        if (methodPermission != null) {
            return Optional.of(PermissionRequirement.from(methodPermission));
        }

        final Class<?> userClass = targetClass != null ? targetClass : method.getDeclaringClass();
        final RequirePermission typePermission =
                AnnotatedElementUtils.findMergedAnnotation(userClass, RequirePermission.class);
        if (typePermission != null) {
            return Optional.of(PermissionRequirement.from(typePermission));
        }

        return Optional.empty();
    }

    public static Optional<RequiresRole> findRoleRequirement(
            final Method method,
            final Class<?> targetClass
    ) {
        final RequiresRole methodRole = AnnotationUtils.findAnnotation(method, RequiresRole.class);
        if (methodRole != null) {
            return Optional.of(methodRole);
        }
        final Class<?> userClass = targetClass != null ? targetClass : method.getDeclaringClass();
        return Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(userClass, RequiresRole.class));
    }

    /**
     * True when the handler requires authentication only (no permission/role code).
     */
    public static boolean requiresAuthenticatedOnly(final Method method, final Class<?> targetClass) {
        if (findPermissionRequirement(method, targetClass).isPresent()
                || findRoleRequirement(method, targetClass).isPresent()) {
            return false;
        }
        if (AnnotationUtils.findAnnotation(method, RequireAuthenticated.class) != null) {
            return true;
        }
        final Class<?> userClass = targetClass != null ? targetClass : method.getDeclaringClass();
        return AnnotatedElementUtils.findMergedAnnotation(userClass, RequireAuthenticated.class) != null;
    }

    public static boolean isPublicEndpoint(final Method method, final Class<?> targetClass) {
        if (AnnotationUtils.findAnnotation(method, PublicEndpoint.class) != null) {
            return true;
        }
        final Class<?> userClass = targetClass != null ? targetClass : method.getDeclaringClass();
        return AnnotatedElementUtils.findMergedAnnotation(userClass, PublicEndpoint.class) != null;
    }

    /**
     * True when the handler declares any access classification (public or protected).
     */
    public static boolean isAccessClassified(final Method method, final Class<?> targetClass) {
        return isPublicEndpoint(method, targetClass)
                || requiresAuthenticatedOnly(method, targetClass)
                || findPermissionRequirement(method, targetClass).isPresent()
                || findRoleRequirement(method, targetClass).isPresent();
    }

    public static boolean hasAuthorizationAnnotation(final AnnotatedElement element) {
        return AnnotatedElementUtils.hasAnnotation(element, RequirePermission.class)
                || AnnotatedElementUtils.hasAnnotation(element, RequiresPermission.class)
                || AnnotatedElementUtils.hasAnnotation(element, RequiresRole.class)
                || AnnotatedElementUtils.hasAnnotation(element, RequireAuthenticated.class)
                || AnnotatedElementUtils.hasAnnotation(element, PublicEndpoint.class);
    }


    /**
     * Normalized permission requirement extracted from either annotation form.
     */
    public record PermissionRequirement(String[] permissions, boolean requireAll) {

        public static PermissionRequirement from(final RequirePermission annotation) {
            return new PermissionRequirement(annotation.value(), annotation.requireAll());
        }

        public static PermissionRequirement from(final RequiresPermission annotation) {
            return new PermissionRequirement(annotation.value(), annotation.requireAll());
        }

        public boolean isEmpty() {
            return permissions == null || permissions.length == 0;
        }
    }
}
