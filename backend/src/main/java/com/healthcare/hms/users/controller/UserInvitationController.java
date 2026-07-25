package com.healthcare.hms.users.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.security.annotation.PublicEndpoint;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.dto.request.AcceptInvitationRequest;
import com.healthcare.hms.users.dto.request.CreateInvitationRequest;
import com.healthcare.hms.users.dto.request.PreviewInvitationRequest;
import com.healthcare.hms.users.dto.request.RejectInvitationRequest;
import com.healthcare.hms.users.dto.response.AcceptInvitationResponse;
import com.healthcare.hms.users.dto.response.InvitationPreviewResponse;
import com.healthcare.hms.users.dto.response.UserInvitationResponse;
import com.healthcare.hms.users.enums.InvitationStatus;
import com.healthcare.hms.users.service.UserInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User invitation APIs (Phase 4.5): invite, resend, cancel (admin) and accept/reject (public token).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "User Invitations", description = "Invite users by email, accept/reject, resend, and cancel")
public class UserInvitationController {

    private final UserInvitationService userInvitationService;

    public UserInvitationController(final UserInvitationService userInvitationService) {
        this.userInvitationService = userInvitationService;
    }

    @PostMapping("/invitations")
    @RequirePermission(PermissionConstants.USER_CREATE)
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "tenantHeader")
    @Operation(
            summary = "Invite user by email",
            description = """
                    Requires JWT, tenant context, and USER_CREATE.
                    Creates a pending invitation, emails a single-use token link, and assigns the
                    selected role when the invitee accepts.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Invitation created and email sent",
                    content = @Content(schema = @Schema(implementation = UserInvitationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate user or pending invite")
    })
    public ResponseEntity<ApiResponse<UserInvitationResponse>> invite(
            @Valid @RequestBody final CreateInvitationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final UserInvitationResponse response = userInvitationService.invite(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation sent successfully", response));
    }

    @GetMapping("/invitations")
    @RequirePermission(PermissionConstants.USER_READ)
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "tenantHeader")
    @Operation(summary = "List invitations", description = "Filter by status and email; paginated.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PageResponse<UserInvitationResponse>>> search(
            @RequestParam(value = "status", required = false) final InvitationStatus status,
            @Parameter(description = "Partial email match")
            @RequestParam(value = "email", required = false) final String email,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) final Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invitations retrieved successfully",
                userInvitationService.search(status, email, pageable)
        ));
    }

    @GetMapping("/invitations/{id}")
    @RequirePermission(PermissionConstants.USER_READ)
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "tenantHeader")
    @Operation(summary = "Get invitation by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Invitation retrieved",
                    content = @Content(schema = @Schema(implementation = UserInvitationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<UserInvitationResponse>> getById(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation retrieved successfully",
                userInvitationService.getById(id)
        ));
    }

    @PostMapping("/invitations/{id}/resend")
    @RequirePermission(PermissionConstants.USER_CREATE)
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "tenantHeader")
    @Operation(summary = "Resend invitation email", description = "Issues a new token and extends expiration.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Invitation resent",
                    content = @Content(schema = @Schema(implementation = UserInvitationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Not pending"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<UserInvitationResponse>> resend(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final UserInvitationResponse response = userInvitationService.resend(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Invitation resent successfully", response));
    }

    @PostMapping("/invitations/{id}/cancel")
    @RequirePermission(PermissionConstants.USER_UPDATE)
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "tenantHeader")
    @Operation(summary = "Cancel pending invitation")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Invitation cancelled",
                    content = @Content(schema = @Schema(implementation = UserInvitationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Not pending"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<UserInvitationResponse>> cancel(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final UserInvitationResponse response = userInvitationService.cancel(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Invitation cancelled successfully", response));
    }

    // --- Public token flows (declared before /{id} patterns that could collide on GET) ---

    @PostMapping("/invitations/preview")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Preview invitation by token",
            description = """
                    Public. Validates the token from the request body (never query string) and returns
                    minimized invitation details for the accept UI.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Invitation preview",
                    content = @Content(schema = @Schema(implementation = InvitationPreviewResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<InvitationPreviewResponse>> preview(
            @Valid @RequestBody final PreviewInvitationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Invitation preview retrieved successfully",
                userInvitationService.previewByToken(request.token())
        ));
    }

    @PostMapping("/invitations/accept")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Accept invitation",
            description = """
                    Public. Creates the user account, assigns the invited role, joins the hospital tenant,
                    and marks the invitation accepted. Email is treated as verified via the invite token.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Account created",
                    content = @Content(schema = @Schema(implementation = AcceptInvitationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<ApiResponse<AcceptInvitationResponse>> accept(
            @Valid @RequestBody final AcceptInvitationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final AcceptInvitationResponse response = userInvitationService.accept(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation accepted successfully", response));
    }

    @PostMapping("/invitations/reject")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(summary = "Reject invitation", description = "Public. Marks the invitation rejected using the emailed token.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Invitation rejected",
                    content = @Content(schema = @Schema(implementation = InvitationPreviewResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<InvitationPreviewResponse>> reject(
            @Valid @RequestBody final RejectInvitationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final InvitationPreviewResponse response = userInvitationService.reject(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Invitation rejected successfully", response));
    }
}
