package com.healthcare.hms.config;

import com.healthcare.hms.security.annotation.PublicEndpoint;
import com.healthcare.hms.security.annotation.RequireAuthenticated;
import com.healthcare.hms.security.annotation.RequiresRole;
import com.healthcare.hms.security.authorization.PermissionAnnotationSupport;
import com.healthcare.hms.security.authorization.PermissionAnnotationSupport.PermissionRequirement;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Enriches OpenAPI operations with authorization metadata (Phase 3.4).
 *
 * <ul>
 *   <li>Public endpoints clear security schemes</li>
 *   <li>Protected endpoints document JWT + tenant requirements</li>
 *   <li>Permission / role / authenticated-only annotations appear in description + extensions</li>
 *   <li>Standard 401 / 403 responses are added when missing</li>
 * </ul>
 */
@Component
public class AuthorizationOpenApiCustomizer implements OperationCustomizer {

    static final String EXT_AUTHZ = "x-hms-authorization";
    static final String BEARER_AUTH = "bearerAuth";
    static final String TENANT_HEADER = "tenantHeader";

    @Override
    public Operation customize(final Operation operation, final HandlerMethod handlerMethod) {
        final Method method = handlerMethod.getMethod();
        final Class<?> beanType = handlerMethod.getBeanType();

        if (isPublic(method, beanType)) {
            operation.setSecurity(List.of());
            appendDescription(operation, "Access: **Public** (anonymous). No JWT or tenant header required.");
            putAuthzExtension(operation, Map.of(
                    "access", "public",
                    "jwt", false,
                    "tenant", false,
                    "permissions", List.of(),
                    "roles", List.of()
            ));
            return operation;
        }

        ensureSecuritySchemes(operation);
        ensureErrorResponses(operation);

        final Optional<PermissionRequirement> permission =
                PermissionAnnotationSupport.findPermissionRequirement(method, beanType);
        final Optional<RequiresRole> role =
                PermissionAnnotationSupport.findRoleRequirement(method, beanType);
        final boolean authenticatedOnly =
                PermissionAnnotationSupport.requiresAuthenticatedOnly(method, beanType)
                        || AnnotationUtils.findAnnotation(method, RequireAuthenticated.class) != null
                        || AnnotatedElementUtils.hasAnnotation(beanType, RequireAuthenticated.class);

        final List<String> permissions = permission
                .map(req -> Arrays.stream(req.permissions()).toList())
                .orElse(List.of());
        final List<String> roles = role
                .map(req -> Arrays.stream(req.value()).toList())
                .orElse(List.of());

        final StringBuilder authzDoc = new StringBuilder();
        authzDoc.append("Access: **Protected**.\n");
        authzDoc.append("- JWT: required (Bearer access token; DB-backed principal)\n");
        authzDoc.append("- Tenant: required for hospital-scoped principals (`X-Tenant-ID` must match JWT)\n");
        authzDoc.append("- Role: verified from principal role set");
        if (!roles.isEmpty()) {
            authzDoc.append(" — required: `").append(String.join("`, `", roles)).append('`');
        }
        authzDoc.append('\n');
        if (!permissions.isEmpty()) {
            final String mode = permission.map(PermissionRequirement::requireAll).orElse(false)
                    ? "all of"
                    : "any of";
            authzDoc.append("- Permission: required (").append(mode).append(") `")
                    .append(String.join("`, `", permissions)).append("`\n");
        } else if (authenticatedOnly) {
            authzDoc.append("- Permission: authenticated principal only (self-service)\n");
        } else {
            authzDoc.append("- Permission: authenticated principal (filter-chain)\n");
        }
        authzDoc.append("- Errors: `401` unauthenticated, `403` forbidden (generic body; no secret leakage)");

        appendDescription(operation, authzDoc.toString());

        final Map<String, Object> extension = new LinkedHashMap<>();
        extension.put("access", "protected");
        extension.put("jwt", true);
        extension.put("tenant", true);
        extension.put("permissions", permissions);
        extension.put("roles", roles);
        extension.put("requireAllPermissions", permission.map(PermissionRequirement::requireAll).orElse(false));
        extension.put("authenticatedOnly", permissions.isEmpty() && roles.isEmpty());
        putAuthzExtension(operation, extension);

        return operation;
    }

    private static boolean isPublic(final Method method, final Class<?> beanType) {
        return AnnotationUtils.findAnnotation(method, PublicEndpoint.class) != null
                || AnnotatedElementUtils.hasAnnotation(beanType, PublicEndpoint.class);
    }

    private static void ensureSecuritySchemes(final Operation operation) {
        if (operation.getSecurity() == null || operation.getSecurity().isEmpty()) {
            operation.setSecurity(List.of(
                    new SecurityRequirement().addList(BEARER_AUTH).addList(TENANT_HEADER)
            ));
        }
    }

    private static void ensureErrorResponses(final Operation operation) {
        if (operation.getResponses() == null) {
            return;
        }
        if (!operation.getResponses().containsKey("401")) {
            operation.getResponses().addApiResponse("401", jsonError(
                    "Unauthenticated — missing/invalid JWT or failed credential check"
            ));
        }
        if (!operation.getResponses().containsKey("403")) {
            operation.getResponses().addApiResponse("403", jsonError(
                    "Forbidden — missing permission/role or tenant mismatch (generic message)"
            ));
        }
    }

    private static ApiResponse jsonError(final String description) {
        final Schema<?> schema = new Schema<>().type("object");
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", new MediaType().schema(schema)));
    }

    private static void appendDescription(final Operation operation, final String addition) {
        final String existing = operation.getDescription();
        if (existing == null || existing.isBlank()) {
            operation.setDescription(addition);
        } else {
            operation.setDescription(existing.trim() + "\n\n" + addition);
        }
    }

    private static void putAuthzExtension(final Operation operation, final Map<String, Object> value) {
        operation.addExtension(EXT_AUTHZ, value);
    }
}
