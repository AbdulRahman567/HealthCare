/**
 * RBAC domain primitives for HMS (Phases 3.1 / 3.5).
 *
 * <p>Owns permission naming ({@link PermissionNaming}), the platform
 * {@link PermissionCatalog}, default {@link SystemRolePermissionMatrix},
 * and {@link RoleHierarchy} metadata.
 *
 * <p>Persistence entities remain in {@link com.healthcare.hms.users.entity};
 * runtime authorization enforcement remains in {@code com.healthcare.hms.security}.
 * Platform seeding: {@link com.healthcare.hms.users.bootstrap.PlatformRbacBootstrap}.
 */
package com.healthcare.hms.users.rbac;
