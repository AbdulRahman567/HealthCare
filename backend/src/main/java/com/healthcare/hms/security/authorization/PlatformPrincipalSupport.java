package com.healthcare.hms.security.authorization;

import com.healthcare.hms.common.exception.authorization.RoleDeniedException;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.enums.RoleType;
import java.util.Set;

/**
 * Shared platform-principal checks (tenant-bypass trust bar).
 *
 * <p>Platform Super Admin is the only actor allowed to operate without a bound
 * hospital tenant. Both {@code tenantId == null} and the {@code SUPER_ADMIN} role
 * are required — null tenant alone is never enough.
 */
public final class PlatformPrincipalSupport {

    private PlatformPrincipalSupport() {
    }

    public static boolean isPlatformSuperAdmin(final AuthenticatedUser user) {
        if (user == null) {
            return false;
        }
        return user.getTenantId() == null
                && user.getRoles().contains(RoleType.Names.SUPER_ADMIN);
    }

    /**
     * Enforces platform Super Admin for privileged cross-tenant operations
     * (for example temporarily disabling the Hibernate tenant filter).
     */
    public static void requirePlatformSuperAdmin(final AuthenticatedUser user) {
        if (!isPlatformSuperAdmin(user)) {
            throw new RoleDeniedException(Set.of(RoleType.Names.SUPER_ADMIN), false);
        }
    }
}
