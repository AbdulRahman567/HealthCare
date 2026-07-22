package com.healthcare.hms.tenant.context;

import com.healthcare.hms.tenant.exception.TenantRequiredException;
import java.util.Optional;
import java.util.UUID;

/**
 * Thread-safe holder for the current request's {@link TenantContext}.
 *
 * <p>Uses a plain {@link ThreadLocal} (not inheritable) so pooled servlet threads
 * never leak tenant identity across requests. Always {@link #clear()} in a
 * {@code finally} block from {@link com.healthcare.hms.tenant.web.TenantFilter}.
 *
 * <p>Not safe for unstructured async hand-off without a task decorator that
 * copies and clears the context on worker threads.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(final TenantContext context) {
        if (context == null) {
            clear();
            return;
        }
        CURRENT.set(context);
    }

    public static Optional<TenantContext> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static Optional<UUID> getTenantId() {
        return get().map(TenantContext::tenantId);
    }

    /**
     * Returns the bound tenant id or fails when none is present
     * (platform Super Admin flows should not call this).
     */
    public static UUID requireTenantId() {
        return getTenantId().orElseThrow(TenantRequiredException::new);
    }

    public static boolean isPresent() {
        return CURRENT.get() != null;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
