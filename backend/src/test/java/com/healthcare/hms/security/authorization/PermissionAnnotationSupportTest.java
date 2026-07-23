package com.healthcare.hms.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.annotation.RequiresPermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PermissionAnnotationSupport")
class PermissionAnnotationSupportTest {

    @Test
    @DisplayName("prefers method-level RequirePermission over type-level")
    void methodOverridesType() throws Exception {
        final var requirement = PermissionAnnotationSupport.findPermissionRequirement(
                AnnotatedService.class.getMethod("write"),
                AnnotatedService.class
        );

        assertThat(requirement).isPresent();
        assertThat(requirement.orElseThrow().permissions())
                .containsExactly(PermissionConstants.PATIENT_UPDATE);
        assertThat(requirement.orElseThrow().requireAll()).isFalse();
    }

    @Test
    @DisplayName("supports legacy RequiresPermission")
    void legacyAnnotation() throws Exception {
        final var requirement = PermissionAnnotationSupport.findPermissionRequirement(
                LegacyService.class.getMethod("read"),
                LegacyService.class
        );

        assertThat(requirement).isPresent();
        assertThat(requirement.orElseThrow().permissions())
                .containsExactly(PermissionConstants.HOSPITAL_READ);
    }

    @Test
    @DisplayName("detects RequireAuthenticated when no permission/role is present")
    void authenticatedOnly() throws Exception {
        assertThat(PermissionAnnotationSupport.requiresAuthenticatedOnly(
                SelfService.class.getMethod("profile"),
                SelfService.class
        )).isTrue();

        assertThat(PermissionAnnotationSupport.requiresAuthenticatedOnly(
                AnnotatedService.class.getMethod("write"),
                AnnotatedService.class
        )).isFalse();
    }

    @RequirePermission(PermissionConstants.PATIENT_READ)
    static class AnnotatedService {
        @RequirePermission(PermissionConstants.PATIENT_UPDATE)
        public void write() {
        }
    }

    static class LegacyService {
        @RequiresPermission(PermissionConstants.HOSPITAL_READ)
        public void read() {
        }
    }

    static class SelfService {
        @com.healthcare.hms.security.annotation.RequireAuthenticated
        public void profile() {
        }
    }
}
