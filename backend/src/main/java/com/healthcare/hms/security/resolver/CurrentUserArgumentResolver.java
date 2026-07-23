package com.healthcare.hms.security.resolver;

import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.security.annotation.CurrentUser;
import com.healthcare.hms.security.authorization.CurrentUserAccessor;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link CurrentUser}-annotated controller parameters from the security context.
 *
 * <p>Supports both the {@link com.healthcare.hms.security.principal.CurrentUser} abstraction
 * and the concrete {@link AuthenticatedUser} implementation.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final CurrentUserAccessor currentUserAccessor;

    public CurrentUserArgumentResolver(final CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    @Override
    public boolean supportsParameter(@NonNull final MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(CurrentUser.class)) {
            return false;
        }
        final Class<?> type = parameter.getParameterType();
        return com.healthcare.hms.security.principal.CurrentUser.class.isAssignableFrom(type);
    }

    @Override
    public Object resolveArgument(
            @NonNull final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            @NonNull final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) {
        final CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        final boolean required = annotation == null || annotation.required();

        return currentUserAccessor.findCurrentUser()
                .map(user -> adapt(user, parameter.getParameterType()))
                .orElseGet(() -> {
                    if (required) {
                        throw new UnauthorizedException();
                    }
                    return null;
                });
    }

    private static Object adapt(
            final com.healthcare.hms.security.principal.CurrentUser user,
            final Class<?> targetType
    ) {
        if (targetType.isInstance(user)) {
            return user;
        }
        if (AuthenticatedUser.class.equals(targetType) && user instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }
        throw new IllegalStateException(
                "Cannot resolve @CurrentUser parameter of type " + targetType.getName()
        );
    }
}
