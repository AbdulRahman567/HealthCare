package com.healthcare.hms.common.exception.authorization;

import java.util.Collections;
import java.util.Set;

/**
 * Raised when the caller lacks one or more required permission codes.
 *
 * <p>Client-facing message stays generic; {@link #getRequiredPermissions()} is for
 * server-side logging/diagnostics only (do not echo codes in public API bodies).
 */
public class PermissionDeniedException extends AuthorizationException {

    private final Set<String> requiredPermissions;
    private final boolean requireAll;

    public PermissionDeniedException() {
        this(Set.of(), false);
    }

    public PermissionDeniedException(final Set<String> requiredPermissions, final boolean requireAll) {
        super("AUTHZ_PERMISSION_DENIED", "You do not have permission to perform this action");
        this.requiredPermissions = requiredPermissions == null
                ? Set.of()
                : Collections.unmodifiableSet(Set.copyOf(requiredPermissions));
        this.requireAll = requireAll;
    }

    public Set<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public boolean isRequireAll() {
        return requireAll;
    }
}
