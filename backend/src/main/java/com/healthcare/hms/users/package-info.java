/**
 * Identity and RBAC persistence domain.
 *
 * <p>Entities: {@link com.healthcare.hms.users.entity.User},
 * {@link com.healthcare.hms.users.entity.Role},
 * {@link com.healthcare.hms.users.entity.Permission},
 * {@link com.healthcare.hms.users.entity.UserInvitation} (Phase 4.5).
 *
 * <p>Administration: invitations (4.5), user directory and status lifecycle (4.6).
 *
 * <p>RBAC conventions and hierarchy: {@link com.healthcare.hms.users.rbac}.
 *
 * <p>Runtime authorization annotations remain in {@code com.healthcare.hms.security}.
 */
package com.healthcare.hms.users;
