package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.authorization.PermissionAnnotationSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Startup fail-fast: every {@code /api/**} REST handler must declare an access
 * classification annotation (Phase 3.8).
 */
@Component
public class ControllerAuthorizationCoverageGuard implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ControllerAuthorizationCoverageGuard.class);

    private final ApplicationContext applicationContext;

    public ControllerAuthorizationCoverageGuard(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(final ApplicationArguments args) {
        final RequestMappingHandlerMapping mapping =
                applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);

        final List<String> unclassified = new ArrayList<>();
        for (final var entry : mapping.getHandlerMethods().entrySet()) {
            final RequestMappingInfo info = entry.getKey();
            final HandlerMethod handler = entry.getValue();
            if (!isApiHandler(info, handler)) {
                continue;
            }
            final Method method = handler.getMethod();
            final Class<?> beanType = handler.getBeanType();
            if (!PermissionAnnotationSupport.isAccessClassified(method, beanType)) {
                unclassified.add(beanType.getName() + "#" + method.getName());
            }
        }

        if (!unclassified.isEmpty()) {
            throw new IllegalStateException(
                    "Unclassified /api handlers (add @PublicEndpoint, @RequireAuthenticated, "
                            + "@RequirePermission, or @RequiresRole): "
                            + String.join(", ", unclassified)
            );
        }

        log.info("Controller authorization coverage check passed for /api handlers");
    }

    private static boolean isApiHandler(final RequestMappingInfo info, final HandlerMethod handler) {
        if (AnnotationUtils.findAnnotation(handler.getBeanType(), RestController.class) == null) {
            return false;
        }
        if (info.getPatternValues().stream().anyMatch(pattern -> pattern.startsWith("/api"))) {
            return true;
        }
        final RequestMapping typeMapping =
                AnnotationUtils.findAnnotation(handler.getBeanType(), RequestMapping.class);
        if (typeMapping == null) {
            return false;
        }
        for (final String path : typeMapping.path()) {
            if (path.startsWith("/api")) {
                return true;
            }
        }
        for (final String path : typeMapping.value()) {
            if (path.startsWith("/api")) {
                return true;
            }
        }
        return false;
    }
}
