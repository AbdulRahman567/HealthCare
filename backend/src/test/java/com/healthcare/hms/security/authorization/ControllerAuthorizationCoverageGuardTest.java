package com.healthcare.hms.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ControllerAuthorizationCoverageGuardTest {

    @Test
    @DisplayName("treats /api and /api/... as application API paths")
    void recognizesApplicationApiPaths() {
        assertThat(ControllerAuthorizationCoverageGuard.isApplicationApiPath("/api")).isTrue();
        assertThat(ControllerAuthorizationCoverageGuard.isApplicationApiPath("/api/v1/auth/login")).isTrue();
        assertThat(ControllerAuthorizationCoverageGuard.isApplicationApiPath("/api/v1/system/health")).isTrue();
    }

    @Test
    @DisplayName("does not treat SpringDoc /api-docs as an application API path")
    void excludesApiDocsPaths() {
        assertThat(ControllerAuthorizationCoverageGuard.isApplicationApiPath("/api-docs")).isFalse();
        assertThat(ControllerAuthorizationCoverageGuard.isApplicationApiPath("/api-docs/swagger-config")).isFalse();
        assertThat(ControllerAuthorizationCoverageGuard.isApplicationApiPath("/swagger-ui")).isFalse();
        assertThat(ControllerAuthorizationCoverageGuard.isApplicationApiPath("/actuator/health")).isFalse();
    }
}
