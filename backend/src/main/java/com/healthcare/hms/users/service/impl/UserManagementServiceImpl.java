package com.healthcare.hms.users.service.impl;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.auth.service.RefreshTokenService;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.principal.CurrentUser;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.dto.request.AdminUpdateUserRequest;
import com.healthcare.hms.users.dto.response.UserManagementResponse;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.mapper.UserManagementMapper;
import com.healthcare.hms.users.rbac.RoleHierarchy;
import com.healthcare.hms.users.repository.UserRepository;
import com.healthcare.hms.users.repository.UserSpecifications;
import com.healthcare.hms.users.service.UserAccountLifecycleHook;
import com.healthcare.hms.users.service.UserManagementService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tenant-isolated user administration with status lifecycle (Phase 4.6).
 */
@Service
public class UserManagementServiceImpl implements UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementServiceImpl.class);
    private static final String ENTITY = "USER";
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SORT_PROPERTIES = Set.of(
            "firstName",
            "lastName",
            "email",
            "status",
            "emailVerified",
            "lastLoginAt",
            "createdAt",
            "updatedAt"
    );

    private final UserRepository userRepository;
    private final UserManagementMapper userManagementMapper;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;
    private final List<UserAccountLifecycleHook> accountLifecycleHooks;

    public UserManagementServiceImpl(
            final UserRepository userRepository,
            final UserManagementMapper userManagementMapper,
            final RefreshTokenService refreshTokenService,
            final AuditLogService auditLogService,
            final List<UserAccountLifecycleHook> accountLifecycleHooks
    ) {
        this.userRepository = userRepository;
        this.userManagementMapper = userManagementMapper;
        this.refreshTokenService = refreshTokenService;
        this.auditLogService = auditLogService;
        this.accountLifecycleHooks = accountLifecycleHooks;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.USER_READ)
    public UserManagementResponse getById(final UUID userId) {
        return userManagementMapper.toResponse(requireTenantUserWithRoles(userId));
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.USER_READ)
    public PageResponse<UserManagementResponse> search(
            final String search,
            final UserStatus status,
            final RoleType roleType,
            final Boolean emailVerified,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return PageResponse.from(userRepository
                .findAll(
                        UserSpecifications.withFilters(tenantId, search, status, roleType, emailVerified),
                        sanitizePageable(pageable)
                )
                .map(userManagementMapper::toResponse));
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.USER_UPDATE)
    public UserManagementResponse updateProfile(
            final UUID userId,
            final AdminUpdateUserRequest request,
            final String ipAddress,
            final String userAgent
    ) {
        final User user = requireTenantUserWithRoles(userId);
        if (!SecurityUtils.requireCurrentUser().getUserId().equals(user.getId())) {
            assertActorOutranksTarget(user);
        }
        final String oldSnapshot = snapshot(user);

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(trimToNull(request.phone()));
        final User saved = userRepository.save(user);

        audit(saved, AuditAction.UPDATE, oldSnapshot, ipAddress, userAgent);
        log.info("User profile updated userId={} tenantId={}", saved.getId(), saved.getTenantId());
        return userManagementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.USER_UPDATE)
    public UserManagementResponse activate(
            final UUID userId,
            final String ipAddress,
            final String userAgent
    ) {
        final User user = requireTenantUserWithRoles(userId);
        assertNotSelf(user.getId(), "activate");
        assertActorOutranksTarget(user);
        final String oldSnapshot = snapshot(user);
        try {
            user.activate();
        } catch (final IllegalStateException exception) {
            throw new BusinessException("USER_INVALID_STATUS_TRANSITION", exception.getMessage());
        }
        final User saved = userRepository.save(user);
        audit(saved, AuditAction.UPDATE, oldSnapshot, ipAddress, userAgent);
        log.info("User activated userId={} tenantId={}", saved.getId(), saved.getTenantId());
        return userManagementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.USER_UPDATE)
    public UserManagementResponse deactivate(
            final UUID userId,
            final String ipAddress,
            final String userAgent
    ) {
        final User user = requireTenantUserWithRoles(userId);
        assertNotSelf(user.getId(), "deactivate");
        assertActorOutranksTarget(user);
        assertNotLastActiveHospitalAdmin(user);
        final String oldSnapshot = snapshot(user);
        try {
            user.deactivate();
        } catch (final IllegalStateException exception) {
            throw new BusinessException("USER_INVALID_STATUS_TRANSITION", exception.getMessage());
        }
        invalidateSessions(user);
        notifyAuthenticationDisabled(user);
        final User saved = userRepository.save(user);
        audit(saved, AuditAction.UPDATE, oldSnapshot, ipAddress, userAgent);
        log.info("User deactivated userId={} tenantId={}", saved.getId(), saved.getTenantId());
        return userManagementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.USER_UPDATE)
    public UserManagementResponse suspend(
            final UUID userId,
            final String ipAddress,
            final String userAgent
    ) {
        final User user = requireTenantUserWithRoles(userId);
        assertNotSelf(user.getId(), "suspend");
        assertActorOutranksTarget(user);
        assertNotLastActiveHospitalAdmin(user);
        final String oldSnapshot = snapshot(user);
        try {
            user.suspend();
        } catch (final IllegalStateException exception) {
            throw new BusinessException("USER_INVALID_STATUS_TRANSITION", exception.getMessage());
        }
        invalidateSessions(user);
        notifyAuthenticationDisabled(user);
        final User saved = userRepository.save(user);
        audit(saved, AuditAction.UPDATE, oldSnapshot, ipAddress, userAgent);
        log.info("User suspended userId={} tenantId={}", saved.getId(), saved.getTenantId());
        return userManagementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @RequirePermission(PermissionConstants.USER_UPDATE)
    public UserManagementResponse restore(
            final UUID userId,
            final String ipAddress,
            final String userAgent
    ) {
        final User user = requireTenantUserWithRoles(userId);
        assertNotSelf(user.getId(), "restore");
        assertActorOutranksTarget(user);
        final String oldSnapshot = snapshot(user);
        try {
            user.restore();
        } catch (final IllegalStateException exception) {
            throw new BusinessException("USER_INVALID_STATUS_TRANSITION", exception.getMessage());
        }
        final User saved = userRepository.save(user);
        audit(saved, AuditAction.UPDATE, oldSnapshot, ipAddress, userAgent);
        log.info("User restored userId={} tenantId={}", saved.getId(), saved.getTenantId());
        return userManagementMapper.toResponse(saved);
    }

    private User requireTenantUserWithRoles(final UUID userId) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return userRepository.findByIdAndTenantIdWithRolesAndPermissions(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void assertNotSelf(final UUID targetUserId, final String action) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        if (actorId.equals(targetUserId)) {
            throw new BusinessException(
                    "USER_SELF_STATUS_CHANGE_FORBIDDEN",
                    "You cannot " + action + " your own account"
            );
        }
    }

    /**
     * Actor must strictly outrank the target (prevents peer Hospital Admin takeover).
     */
    private void assertActorOutranksTarget(final User target) {
        final CurrentUser actor = SecurityUtils.requireCurrentUser();
        final int actorLevel = privilegeLevel(actor.getRoles());
        final int targetLevel = privilegeLevel(target.getRoles().stream()
                .map(Role::getType)
                .map(Enum::name)
                .collect(Collectors.toSet()));
        if (actorLevel >= targetLevel) {
            throw new BusinessException(
                    "USER_INSUFFICIENT_RANK",
                    "You cannot change the status of a user with equal or higher privilege"
            );
        }
    }

    private void assertNotLastActiveHospitalAdmin(final User target) {
        final boolean isHospitalAdmin = target.getRoles().stream()
                .anyMatch(role -> role.getType() == RoleType.HOSPITAL_ADMIN);
        if (!isHospitalAdmin || target.getStatus() != UserStatus.ACTIVE) {
            return;
        }
        final long activeAdmins = userRepository.countByTenantIdAndRoleTypeAndStatus(
                target.getTenantId(),
                RoleType.HOSPITAL_ADMIN,
                UserStatus.ACTIVE
        );
        if (activeAdmins <= 1) {
            throw new BusinessException(
                    "USER_LAST_HOSPITAL_ADMIN",
                    "Cannot deactivate or suspend the last active hospital administrator"
            );
        }
    }

    private static int privilegeLevel(final Set<String> roleNames) {
        return roleNames.stream()
                .map(UserManagementServiceImpl::levelOfRoleName)
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);
    }

    private static int levelOfRoleName(final String roleName) {
        try {
            return RoleHierarchy.levelOf(RoleType.valueOf(roleName));
        } catch (final IllegalArgumentException exception) {
            return Integer.MAX_VALUE;
        }
    }

    private void invalidateSessions(final User user) {
        user.incrementTokenVersion();
        refreshTokenService.revokeAllForUser(user.getId());
    }

    private void notifyAuthenticationDisabled(final User user) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        for (final UserAccountLifecycleHook hook : accountLifecycleHooks) {
            hook.onUserAuthenticationDisabled(user.getTenantId(), user.getId(), actorId);
        }
    }

    private void audit(
            final User user,
            final AuditAction action,
            final String oldSnapshot,
            final String ipAddress,
            final String userAgent
    ) {
        final UUID actorId = SecurityUtils.requireCurrentUser().getUserId();
        auditLogService.record(
                user.getTenantId(),
                actorId,
                ENTITY,
                user.getId().toString(),
                action,
                oldSnapshot,
                snapshot(user),
                ipAddress,
                userAgent
        );
    }

    private static Pageable sanitizePageable(final Pageable pageable) {
        final int page = Math.max(pageable.getPageNumber(), 0);
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName", "firstName"));
        }
        final Sort safeSort = Sort.by(pageable.getSort().stream()
                .filter(order -> SORT_PROPERTIES.contains(order.getProperty()))
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .toList());
        if (safeSort.isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName", "firstName"));
        }
        return PageRequest.of(page, size, safeSort);
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String snapshot(final User user) {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", user.getId());
        fields.put("email", user.getEmail());
        fields.put("firstName", user.getFirstName());
        fields.put("lastName", user.getLastName());
        fields.put("phone", user.getPhone());
        fields.put("status", user.getStatus());
        fields.put("emailVerified", user.isEmailVerified());
        fields.put("tokenVersion", user.getTokenVersion());
        return fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
