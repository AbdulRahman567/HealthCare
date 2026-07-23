/**
 * Identity and RBAC persistence domain.
 *
 * <p>Entities: {@link com.healthcare.hms.users.entity.User},
 * {@link com.healthcare.hms.users.entity.Role},
 * {@link com.healthcare.hms.users.entity.Permission}.
 *
 * <p>RBAC conventions and hierarchy: {@link com.healthcare.hms.users.rbac}.
 *
 * <p>Runtime authorization annotations remain in {@code com.healthcare.hms.security}
 * (enforcement refinements are out of scope for Phase 3.1 domain design).
 */
package com.healthcare.hms.users;
