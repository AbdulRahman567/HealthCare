package com.healthcare.hms.common.exception.authorization;

import java.util.Collections;
import java.util.Set;

/**
 * Raised when the caller lacks one or more required roles.
 *
 * <p>Client-facing message stays generic; role names are retained for diagnostics only.
 */
public class RoleDeniedException extends AuthorizationException {

    private final Set<String> requiredRoles;
    private final boolean requireAll;

    public RoleDeniedException() {
        this(Set.of(), false);
    }

    public RoleDeniedException(final Set<String> requiredRoles, final boolean requireAll) {
        super("AUTHZ_ROLE_DENIED", "You do not have permission to perform this action");
        this.requiredRoles = requiredRoles == null
                ? Set.of()
                : Collections.unmodifiableSet(Set.copyOf(requiredRoles));
        this.requireAll = requireAll;
    }

    public Set<String> getRequiredRoles() {
        return requiredRoles;
    }

    public boolean isRequireAll() {
        return requireAll;
    }
}
