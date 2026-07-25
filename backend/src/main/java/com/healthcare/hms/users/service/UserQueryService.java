package com.healthcare.hms.users.service;

import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.RoleType;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-side user lookups for other modules (keeps repository access inside users).
 */
public interface UserQueryService {

    Optional<User> findById(UUID userId);

    Optional<User> findByIdWithRoles(UUID userId);

    /**
     * Loads the user and asserts tenant membership plus expected role type.
     *
     * @throws com.healthcare.hms.common.exception.BusinessException on mismatch
     * @throws com.healthcare.hms.common.exception.ResourceNotFoundException if missing
     */
    User requireTenantUserWithRole(UUID tenantId, UUID userId, RoleType expectedRole);
}
