package com.healthcare.hms.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.healthcare.hms.common.exception.authorization.PermissionDeniedException;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionGuard")
class PermissionGuardTest {

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private PermissionGuard permissionGuard;

    @Test
    @DisplayName("enforce RequirePermission requireAll=false delegates to requireAny")
    void enforceAny() {
        final RequirePermission annotation = annotation(false, PermissionConstants.PATIENT_READ);

        permissionGuard.enforce(annotation);

        verify(authorizationService).requireAnyPermission(PermissionConstants.PATIENT_READ);
        verifyNoMoreInteractions(authorizationService);
    }

    @Test
    @DisplayName("enforce RequirePermission requireAll=true delegates to requireAll")
    void enforceAll() {
        final RequirePermission annotation = annotation(
                true,
                PermissionConstants.PATIENT_READ,
                PermissionConstants.VISIT_UPDATE
        );

        permissionGuard.enforce(annotation);

        verify(authorizationService).requireAllPermissions(
                PermissionConstants.PATIENT_READ,
                PermissionConstants.VISIT_UPDATE
        );
    }

    @Test
    @DisplayName("allowsAny proxies AuthorizationService")
    void allowsAny() {
        when(authorizationService.hasAnyPermission(PermissionConstants.AUDIT_READ)).thenReturn(true);
        assertThat(permissionGuard.allowsAny(PermissionConstants.AUDIT_READ)).isTrue();
    }

    @Test
    @DisplayName("requireAny surfaces permission denial from AuthorizationService")
    void requireAny_denied() {
        org.mockito.Mockito.doThrow(new PermissionDeniedException())
                .when(authorizationService)
                .requireAnyPermission(PermissionConstants.ROLE_DELETE);

        assertThatThrownBy(() -> permissionGuard.requireAny(PermissionConstants.ROLE_DELETE))
                .isInstanceOf(PermissionDeniedException.class);
    }

    private static RequirePermission annotation(final boolean requireAll, final String... values) {
        return new RequirePermission() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return RequirePermission.class;
            }

            @Override
            public String[] value() {
                return values;
            }

            @Override
            public boolean requireAll() {
                return requireAll;
            }
        };
    }
}
