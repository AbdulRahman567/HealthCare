package com.healthcare.hms.security.authorization;

import com.healthcare.hms.common.exception.authorization.MissingAuthorizationAnnotationException;
import com.healthcare.hms.security.annotation.RequiresRole;
import com.healthcare.hms.security.authorization.PermissionAnnotationSupport.PermissionRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP-layer authorization middleware for controller handlers.
 *
 * <p>Phase 3.8 fail-closed policy: every {@code /api/**} {@link HandlerMethod} must declare
 * {@link com.healthcare.hms.security.annotation.PublicEndpoint},
 * {@link com.healthcare.hms.security.annotation.RequireAuthenticated},
 * {@link com.healthcare.hms.security.annotation.RequirePermission} (or legacy
 * {@link com.healthcare.hms.security.annotation.RequiresPermission}), or
 * {@link RequiresRole}. Unclassified handlers are denied.
 */
@Component
public class PermissionAuthorizationInterceptor implements HandlerInterceptor {

    private final PermissionGuard permissionGuard;

    public PermissionAuthorizationInterceptor(final PermissionGuard permissionGuard) {
        this.permissionGuard = permissionGuard;
    }

    @Override
    public boolean preHandle(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        final var method = handlerMethod.getMethod();
        final var beanType = handlerMethod.getBeanType();

        if (PermissionAnnotationSupport.isPublicEndpoint(method, beanType)) {
            return true;
        }

        final Optional<PermissionRequirement> permission =
                PermissionAnnotationSupport.findPermissionRequirement(method, beanType);
        final Optional<RequiresRole> role =
                PermissionAnnotationSupport.findRoleRequirement(method, beanType);
        final boolean authenticatedOnly =
                PermissionAnnotationSupport.requiresAuthenticatedOnly(method, beanType);

        if (permission.isEmpty() && role.isEmpty() && !authenticatedOnly) {
            throw new MissingAuthorizationAnnotationException(
                    beanType.getSimpleName() + "#" + method.getName()
            );
        }

        permissionGuard.requireAuthenticated();
        permission.ifPresent(permissionGuard::enforce);
        role.ifPresent(permissionGuard::enforce);
        return true;
    }
}
