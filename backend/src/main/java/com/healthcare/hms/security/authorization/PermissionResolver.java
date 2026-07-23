package com.healthcare.hms.security.authorization;

import com.healthcare.hms.security.principal.CurrentUser;
import com.healthcare.hms.users.entity.User;
import java.util.Set;

/**
 * Resolves effective role and permission codes for authorization decisions.
 *
 * <p>Implementations must be stateless and thread-safe. Runtime checks should use the
 * immutable snapshot on {@link CurrentUser}; persistence-backed resolution is for
 * principal construction (e.g. JWT validation).
 */
public interface PermissionResolver {

    /**
     * Effective permission codes already attached to the principal snapshot.
     */
    Set<String> resolvePermissions(CurrentUser user);

    /**
     * Effective role type names already attached to the principal snapshot.
     */
    Set<String> resolveRoles(CurrentUser user);

    /**
     * Derives permission codes from a persisted user and their role grants.
     */
    Set<String> resolvePermissions(User user);

    /**
     * Derives role type names from a persisted user.
     */
    Set<String> resolveRoles(User user);
}
