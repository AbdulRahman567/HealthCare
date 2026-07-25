package com.healthcare.hms.users.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.auth.service.RefreshTokenService;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.users.dto.request.AdminUpdateUserRequest;
import com.healthcare.hms.users.dto.response.UserManagementResponse;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.mapper.UserManagementMapper;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserManagementServiceImpl")
class UserManagementServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserManagementMapper userManagementMapper;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuditLogService auditLogService;

    private UserManagementServiceImpl service;
    private UUID tenantId;
    private UUID actorId;
    private UUID targetUserId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        service = new UserManagementServiceImpl(
                userRepository,
                userManagementMapper,
                refreshTokenService,
                auditLogService,
                java.util.List.of()
        );

        TenantContextHolder.set(new TenantContext(tenantId, "city", TenantType.HOSPITAL, TenantStatus.ACTIVE));
        final AuthenticatedUser principal = new AuthenticatedUser(
                actorId,
                tenantId,
                "admin@city.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("USER_READ", "USER_UPDATE"),
                1L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("deactivate moves ACTIVE to INACTIVE and revokes sessions")
    void deactivate_success() {
        final User user = activeUser(targetUserId);
        final UserManagementResponse mapped = sampleResponse(UserStatus.INACTIVE);

        when(userRepository.findByIdAndTenantIdWithRolesAndPermissions(targetUserId, tenantId))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userManagementMapper.toResponse(user)).thenReturn(mapped);

        final UserManagementResponse result = service.deactivate(targetUserId, "127.0.0.1", "junit");

        assertThat(result.status()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getTokenVersion()).isEqualTo(1L);
        verify(refreshTokenService).revokeAllForUser(targetUserId);
        verify(auditLogService).record(
                eq(tenantId),
                eq(actorId),
                eq("USER"),
                eq(targetUserId.toString()),
                eq(AuditAction.UPDATE),
                any(),
                any(),
                eq("127.0.0.1"),
                eq("junit")
        );
    }

    @Test
    @DisplayName("suspend moves ACTIVE to SUSPENDED")
    void suspend_success() {
        final User user = activeUser(targetUserId);
        when(userRepository.findByIdAndTenantIdWithRolesAndPermissions(targetUserId, tenantId))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userManagementMapper.toResponse(user))
                .thenReturn(sampleResponse(UserStatus.SUSPENDED));

        service.suspend(targetUserId, "127.0.0.1", "junit");

        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        verify(refreshTokenService).revokeAllForUser(targetUserId);
    }

    @Test
    @DisplayName("restore moves SUSPENDED to ACTIVE")
    void restore_fromSuspended_success() {
        final User user = activeUser(targetUserId);
        user.setStatus(UserStatus.SUSPENDED);
        when(userRepository.findByIdAndTenantIdWithRolesAndPermissions(targetUserId, tenantId))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userManagementMapper.toResponse(user))
                .thenReturn(sampleResponse(UserStatus.ACTIVE));

        service.restore(targetUserId, "127.0.0.1", "junit");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(refreshTokenService, never()).revokeAllForUser(any());
    }

    @Test
    @DisplayName("cannot deactivate own account")
    void deactivate_self_forbidden() {
        final User user = activeUser(actorId);
        when(userRepository.findByIdAndTenantIdWithRolesAndPermissions(actorId, tenantId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.deactivate(actorId, "127.0.0.1", "junit"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo("USER_SELF_STATUS_CHANGE_FORBIDDEN");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("update profile changes name and phone")
    void updateProfile_success() {
        final User user = activeUser(targetUserId);
        when(userRepository.findByIdAndTenantIdWithRolesAndPermissions(targetUserId, tenantId))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userManagementMapper.toResponse(user))
                .thenReturn(sampleResponse(UserStatus.ACTIVE));

        service.updateProfile(
                targetUserId,
                new AdminUpdateUserRequest("New", "Name", "555-0100"),
                "127.0.0.1",
                "junit"
        );

        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("Name");
        assertThat(user.getPhone()).isEqualTo("555-0100");
    }

    @Test
    @DisplayName("activate rejects invalid transition from SUSPENDED")
    void activate_fromSuspended_throws() {
        final User user = activeUser(targetUserId);
        user.setStatus(UserStatus.SUSPENDED);
        when(userRepository.findByIdAndTenantIdWithRolesAndPermissions(targetUserId, tenantId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.activate(targetUserId, "127.0.0.1", "junit"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo("USER_INVALID_STATUS_TRANSITION");
    }

    @Test
    @DisplayName("cannot suspend peer hospital admin")
    void suspend_peerAdmin_forbidden() {
        final User peer = activeUser(targetUserId);
        final Role adminRole = new Role();
        adminRole.setType(RoleType.HOSPITAL_ADMIN);
        peer.addRole(adminRole);

        when(userRepository.findByIdAndTenantIdWithRolesAndPermissions(targetUserId, tenantId))
                .thenReturn(Optional.of(peer));

        assertThatThrownBy(() -> service.suspend(targetUserId, "127.0.0.1", "junit"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo("USER_INSUFFICIENT_RANK");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("restore rejects LOCKED security status")
    void restore_fromLocked_throws() {
        final User user = activeUser(targetUserId);
        user.setStatus(UserStatus.LOCKED);
        when(userRepository.findByIdAndTenantIdWithRolesAndPermissions(targetUserId, tenantId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.restore(targetUserId, "127.0.0.1", "junit"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo("USER_INVALID_STATUS_TRANSITION");
    }

    private User activeUser(final UUID id) {
        final User user = new User();
        user.setId(id);
        user.setTenantId(tenantId);
        user.setFirstName("Pat");
        user.setLastName("User");
        user.setEmail("pat@city.test");
        user.setPasswordHash("hash");
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        return user;
    }

    private UserManagementResponse sampleResponse(final UserStatus status) {
        return new UserManagementResponse(
                targetUserId,
                tenantId,
                "Pat",
                "User",
                "pat@city.test",
                null,
                true,
                null,
                status,
                Set.of("DOCTOR"),
                null,
                null,
                null,
                null,
                null,
                0L
        );
    }
}
