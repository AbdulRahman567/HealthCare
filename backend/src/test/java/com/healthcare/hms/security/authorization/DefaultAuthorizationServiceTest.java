package com.healthcare.hms.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.common.exception.authorization.PermissionDeniedException;
import com.healthcare.hms.common.exception.authorization.RoleDeniedException;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.enums.RoleType;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultAuthorizationService")
class DefaultAuthorizationServiceTest {

    @Mock
    private CurrentUserAccessor currentUserAccessor;

    private DefaultAuthorizationService authorizationService;
    private AuthenticatedUser doctor;

    @BeforeEach
    void setUp() {
        final PermissionResolver permissionResolver = new DefaultPermissionResolver();
        final PermissionEvaluator permissionEvaluator = new DefaultPermissionEvaluator(permissionResolver);
        authorizationService = new DefaultAuthorizationService(
                currentUserAccessor,
                permissionEvaluator,
                permissionResolver
        );
        doctor = new AuthenticatedUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "doctor@hospital.test",
                Set.of(RoleType.DOCTOR.name()),
                Set.of(PermissionConstants.PATIENT_READ, PermissionConstants.VISIT_UPDATE),
                0L
        );
    }

    @Test
    @DisplayName("hasPermission returns false when unauthenticated")
    void hasPermission_unauthenticated() {
        when(currentUserAccessor.findCurrentUser()).thenReturn(Optional.empty());
        assertThat(authorizationService.hasPermission(PermissionConstants.PATIENT_READ)).isFalse();
    }

    @Test
    @DisplayName("hasPermission and hasRole succeed for granted codes")
    void hasPermissionAndRole_granted() {
        when(currentUserAccessor.findCurrentUser()).thenReturn(Optional.of(doctor));
        assertThat(authorizationService.hasPermission(PermissionConstants.PATIENT_READ)).isTrue();
        assertThat(authorizationService.hasPermission(PermissionConstants.PATIENT_DELETE)).isFalse();
        assertThat(authorizationService.hasRole(RoleType.DOCTOR.name())).isTrue();
        assertThat(authorizationService.hasRole(RoleType.HOSPITAL_ADMIN.name())).isFalse();
        assertThat(authorizationService.hasAnyPermission(
                PermissionConstants.PATIENT_DELETE,
                PermissionConstants.VISIT_UPDATE
        )).isTrue();
        assertThat(authorizationService.hasAllPermissions(
                PermissionConstants.PATIENT_READ,
                PermissionConstants.VISIT_UPDATE
        )).isTrue();
    }

    @Test
    @DisplayName("requireAnyPermission throws PermissionDeniedException")
    void requireAnyPermission_denied() {
        when(currentUserAccessor.requireCurrentUser()).thenReturn(doctor);

        assertThatThrownBy(() -> authorizationService.requireAnyPermission(PermissionConstants.AUDIT_READ))
                .isInstanceOf(PermissionDeniedException.class)
                .satisfies(ex -> {
                    final PermissionDeniedException denied = (PermissionDeniedException) ex;
                    assertThat(denied.getRequiredPermissions()).contains(PermissionConstants.AUDIT_READ);
                    assertThat(denied.isRequireAll()).isFalse();
                });
    }

    @Test
    @DisplayName("requireAnyRole throws RoleDeniedException")
    void requireAnyRole_denied() {
        when(currentUserAccessor.requireCurrentUser()).thenReturn(doctor);

        assertThatThrownBy(() -> authorizationService.requireAnyRole(RoleType.HOSPITAL_ADMIN.name()))
                .isInstanceOf(RoleDeniedException.class);
    }

    @Test
    @DisplayName("requireAuthenticated fails closed without principal")
    void requireAuthenticated_missing() {
        when(currentUserAccessor.requireCurrentUser()).thenThrow(new UnauthorizedException());
        assertThatThrownBy(authorizationService::requireAuthenticated)
                .isInstanceOf(UnauthorizedException.class);
    }
}
