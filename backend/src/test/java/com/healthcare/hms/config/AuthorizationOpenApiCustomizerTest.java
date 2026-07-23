package com.healthcare.hms.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.security.annotation.PublicEndpoint;
import com.healthcare.hms.security.annotation.RequireAuthenticated;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

@DisplayName("AuthorizationOpenApiCustomizer")
class AuthorizationOpenApiCustomizerTest {

    private final AuthorizationOpenApiCustomizer customizer = new AuthorizationOpenApiCustomizer();

    @Test
    @DisplayName("clears security for public endpoints")
    void publicEndpoint_clearsSecurity() throws Exception {
        final Operation operation = new Operation().responses(new ApiResponses());
        final HandlerMethod handler =
                new HandlerMethod(new DocsController(), DocsController.class.getMethod("login"));

        customizer.customize(operation, handler);

        assertThat(operation.getSecurity()).isEmpty();
        assertThat(operation.getDescription()).contains("Public");
        @SuppressWarnings("unchecked")
        final Map<String, Object> ext =
                (Map<String, Object>) operation.getExtensions().get(AuthorizationOpenApiCustomizer.EXT_AUTHZ);
        assertThat(ext.get("access")).isEqualTo("public");
        assertThat(ext.get("jwt")).isEqualTo(false);
    }

    @Test
    @DisplayName("documents JWT, tenant, and permission for protected endpoints")
    void protectedEndpoint_documentsAuthz() throws Exception {
        final Operation operation = new Operation().responses(new ApiResponses());
        final HandlerMethod handler =
                new HandlerMethod(new DocsController(), DocsController.class.getMethod("settings"));

        customizer.customize(operation, handler);

        assertThat(operation.getSecurity()).isNotEmpty();
        assertThat(operation.getDescription()).contains("HOSPITAL_READ");
        assertThat(operation.getResponses()).containsKeys("401", "403");
        @SuppressWarnings("unchecked")
        final Map<String, Object> ext =
                (Map<String, Object>) operation.getExtensions().get(AuthorizationOpenApiCustomizer.EXT_AUTHZ);
        assertThat(ext.get("access")).isEqualTo("protected");
        assertThat(ext.get("jwt")).isEqualTo(true);
        assertThat(ext.get("permissions")).asList().contains(PermissionConstants.HOSPITAL_READ);
    }

    @Test
    @DisplayName("documents authenticated-only self-service")
    void authenticatedOnly_documentsSelfService() throws Exception {
        final Operation operation = new Operation().responses(new ApiResponses());
        final HandlerMethod handler =
                new HandlerMethod(new DocsController(), DocsController.class.getMethod("profile"));

        customizer.customize(operation, handler);

        assertThat(operation.getDescription()).contains("self-service");
        @SuppressWarnings("unchecked")
        final Map<String, Object> ext =
                (Map<String, Object>) operation.getExtensions().get(AuthorizationOpenApiCustomizer.EXT_AUTHZ);
        assertThat(ext.get("authenticatedOnly")).isEqualTo(true);
    }

    @RestController
    static class DocsController {
        @PublicEndpoint
        @GetMapping("/login")
        public void login() {
        }

        @RequirePermission(PermissionConstants.HOSPITAL_READ)
        @GetMapping("/settings")
        public void settings() {
        }

        @RequireAuthenticated
        @GetMapping("/profile")
        public void profile() {
        }
    }
}
