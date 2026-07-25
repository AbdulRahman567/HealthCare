package com.healthcare.hms.users.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.dto.request.AdminUpdateUserRequest;
import com.healthcare.hms.users.dto.response.UserManagementResponse;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenant-scoped user management APIs (Phase 4.6).
 *
 * <p>No physical delete endpoints — use activate / deactivate / suspend / restore.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Hospital user directory and lifecycle management")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(final UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.USER_READ)
    @Operation(
            summary = "Search users",
            description = """
                    Paginated tenant user directory with optional search (name, email, phone),
                    status / role / emailVerified filters, and whitelisted sorting.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PageResponse<UserManagementResponse>>> search(
            @Parameter(description = "Search text matched against first name, last name, email, phone")
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "status", required = false) final UserStatus status,
            @RequestParam(value = "roleType", required = false) final RoleType roleType,
            @RequestParam(value = "emailVerified", required = false) final Boolean emailVerified,
            @PageableDefault(size = 20, sort = {"lastName", "firstName"}, direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Users retrieved successfully",
                userManagementService.search(q, status, roleType, emailVerified, pageable)
        ));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.USER_READ)
    @Operation(summary = "Get user by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User retrieved",
                    content = @Content(schema = @Schema(implementation = UserManagementResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> getById(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "User retrieved successfully",
                userManagementService.getById(id)
        ));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.USER_UPDATE)
    @Operation(
            summary = "Update user profile",
            description = "Updates first name, last name, and phone. Email and roles are unchanged."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile updated",
                    content = @Content(schema = @Schema(implementation = UserManagementResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> updateProfile(
            @PathVariable("id") final UUID id,
            @Valid @RequestBody final AdminUpdateUserRequest request,
            final HttpServletRequest httpRequest
    ) {
        final UserManagementResponse response = userManagementService.updateProfile(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", response));
    }

    @PostMapping("/{id}/activate")
    @RequirePermission(PermissionConstants.USER_UPDATE)
    @Operation(summary = "Activate user", description = "PENDING or INACTIVE → ACTIVE. Cannot target self.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Activated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> activate(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "User activated successfully",
                userManagementService.activate(
                        id,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @PostMapping("/{id}/deactivate")
    @RequirePermission(PermissionConstants.USER_UPDATE)
    @Operation(
            summary = "Deactivate user",
            description = "ACTIVE → INACTIVE. Revokes sessions. No physical deletion. Cannot target self."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> deactivate(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "User deactivated successfully",
                userManagementService.deactivate(
                        id,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @PostMapping("/{id}/suspend")
    @RequirePermission(PermissionConstants.USER_UPDATE)
    @Operation(
            summary = "Suspend user",
            description = "ACTIVE → SUSPENDED. Revokes sessions. Cannot target self."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suspended"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> suspend(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "User suspended successfully",
                userManagementService.suspend(
                        id,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @PostMapping("/{id}/restore")
    @RequirePermission(PermissionConstants.USER_UPDATE)
    @Operation(
            summary = "Restore user",
            description = "INACTIVE or SUSPENDED → ACTIVE. LOCKED cannot be cleared here. Cannot target self."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Restored"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> restore(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "User restored successfully",
                userManagementService.restore(
                        id,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }
}
