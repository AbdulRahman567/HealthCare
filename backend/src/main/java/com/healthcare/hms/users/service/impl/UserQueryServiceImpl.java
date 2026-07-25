package com.healthcare.hms.users.service.impl;

import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.repository.UserRepository;
import com.healthcare.hms.users.service.UserQueryService;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(final UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByIdWithRoles(final UUID userId) {
        return userRepository.findByIdWithRolesAndPermissions(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public User requireTenantUserWithRole(final UUID tenantId, final UUID userId, final RoleType expectedRole) {
        final User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!Objects.equals(tenantId, user.getTenantId())) {
            throw new BusinessException("STAFF_USER_TENANT_MISMATCH", "User must belong to the current tenant");
        }
        final boolean hasRole = user.getRoles().stream()
                .map(Role::getType)
                .anyMatch(type -> type == expectedRole);
        if (!hasRole) {
            throw new BusinessException(
                    "STAFF_USER_ROLE_MISMATCH",
                    "User must hold role " + expectedRole.name() + " for this staff profile"
            );
        }
        return user;
    }
}
