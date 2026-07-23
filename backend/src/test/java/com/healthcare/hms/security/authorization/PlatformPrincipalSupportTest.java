package com.healthcare.hms.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.common.exception.authorization.RoleDeniedException;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.enums.RoleType;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PlatformPrincipalSupport")
class PlatformPrincipalSupportTest {

    @Test
    @DisplayName("requires null tenant and SUPER_ADMIN together")
    void requiresBothSignals() {
        assertThat(PlatformPrincipalSupport.isPlatformSuperAdmin(null)).isFalse();

        final AuthenticatedUser hospitalSuper = new AuthenticatedUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "admin@hospital.test",
                Set.of(RoleType.Names.SUPER_ADMIN),
                Set.of(),
                1
        );
        assertThat(PlatformPrincipalSupport.isPlatformSuperAdmin(hospitalSuper)).isFalse();

        final AuthenticatedUser nullTenantAdmin = new AuthenticatedUser(
                UUID.randomUUID(),
                null,
                "admin@hospital.test",
                Set.of(RoleType.Names.HOSPITAL_ADMIN),
                Set.of(),
                1
        );
        assertThat(PlatformPrincipalSupport.isPlatformSuperAdmin(nullTenantAdmin)).isFalse();

        final AuthenticatedUser platformSuper = new AuthenticatedUser(
                UUID.randomUUID(),
                null,
                "super@platform.test",
                Set.of(RoleType.Names.SUPER_ADMIN),
                Set.of(),
                1
        );
        assertThat(PlatformPrincipalSupport.isPlatformSuperAdmin(platformSuper)).isTrue();
    }

    @Test
    @DisplayName("requirePlatformSuperAdmin throws RoleDeniedException when denied")
    void requireThrowsWhenDenied() {
        final AuthenticatedUser hospitalAdmin = new AuthenticatedUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "admin@hospital.test",
                Set.of(RoleType.Names.HOSPITAL_ADMIN),
                Set.of(),
                1
        );

        assertThatThrownBy(() -> PlatformPrincipalSupport.requirePlatformSuperAdmin(hospitalAdmin))
                .isInstanceOf(RoleDeniedException.class);
    }
}
