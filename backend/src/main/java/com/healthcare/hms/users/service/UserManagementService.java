package com.healthcare.hms.users.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.users.dto.request.AdminUpdateUserRequest;
import com.healthcare.hms.users.dto.response.UserManagementResponse;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Tenant-scoped user administration (Phase 4.6).
 *
 * <p>No physical deletion — lifecycle is status-driven
 * (activate / deactivate / suspend / restore).
 */
public interface UserManagementService {

    UserManagementResponse getById(UUID userId);

    PageResponse<UserManagementResponse> search(
            String search,
            UserStatus status,
            RoleType roleType,
            Boolean emailVerified,
            Pageable pageable
    );

    UserManagementResponse updateProfile(
            UUID userId,
            AdminUpdateUserRequest request,
            String ipAddress,
            String userAgent
    );

    UserManagementResponse activate(UUID userId, String ipAddress, String userAgent);

    UserManagementResponse deactivate(UUID userId, String ipAddress, String userAgent);

    UserManagementResponse suspend(UUID userId, String ipAddress, String userAgent);

    UserManagementResponse restore(UUID userId, String ipAddress, String userAgent);
}
