package com.healthcare.hms.auth.controller;

import com.healthcare.hms.auth.dto.request.ChangePasswordRequest;
import com.healthcare.hms.auth.dto.request.ForgotPasswordRequest;
import com.healthcare.hms.auth.dto.request.LoginRequest;
import com.healthcare.hms.auth.dto.request.LogoutRequest;
import com.healthcare.hms.auth.dto.request.RefreshTokenRequest;
import com.healthcare.hms.auth.dto.request.RegisterAdminRequest;
import com.healthcare.hms.auth.dto.request.ResendVerificationRequest;
import com.healthcare.hms.auth.dto.request.ResetPasswordRequest;
import com.healthcare.hms.auth.dto.request.UpdateProfileRequest;
import com.healthcare.hms.auth.dto.request.VerifyEmailRequest;
import com.healthcare.hms.auth.dto.response.AuthResponse;
import com.healthcare.hms.auth.dto.response.UserProfileResponse;
import com.healthcare.hms.auth.service.AuthService;
import com.healthcare.hms.common.api.ApiErrorResponse;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.security.annotation.CurrentUser;
import com.healthcare.hms.security.annotation.PublicEndpoint;
import com.healthcare.hms.security.annotation.RequireAuthenticated;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication and session APIs.
 *
 * <p>Public endpoints are anonymous ({@link PublicEndpoint}). Protected endpoints require a
 * verified JWT principal; self-service uses {@link RequireAuthenticated}, resource APIs use
 * {@link RequirePermission}. Tenant binding is enforced by the security filter chain.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, hospital onboarding, and profile management")
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Authenticate user and issue access plus refresh tokens",
            description = "Public. Returns JWT access and refresh tokens after credential verification."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials, inactive account, or unverified email"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody final LoginRequest request,
            final HttpServletRequest httpRequest
    ) {
        final AuthResponse response = authService.login(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register/hospital")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Register a new hospital tenant (compatibility)",
            description = """
                    Public. Delegates to atomic hospital registration.
                    Prefer POST /api/v1/hospitals/register.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Hospital registered"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Hospital or administrator email already exists"
            )
    })
    public ResponseEntity<ApiResponse<HospitalRegistrationResponse>> registerHospital(
            @Valid @RequestBody final HospitalRegistrationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final HospitalRegistrationResponse response = authService.registerHospital(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Hospital registered successfully. Please verify the administrator email before signing in",
                        response
                ));
    }

    @PostMapping("/register/admin")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Legacy initial admin registration (disabled)",
            description = """
                    Public stub returning HTTP 410 Gone. Prefer atomic
                    POST /api/v1/hospitals/register which creates tenant + admin together.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "410",
                    description = "Endpoint permanently disabled"
            )
    })
    public ResponseEntity<ApiErrorResponse> registerInitialAdmin(
            @Valid @RequestBody final RegisterAdminRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ApiErrorResponse.of(
                        "Legacy initial-admin registration is disabled. Use POST /api/v1/hospitals/register",
                        "ENDPOINT_DISABLED",
                        httpRequest.getRequestURI()
                ));
    }

    @PostMapping("/refresh-token")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Rotate refresh token and issue a new access token",
            description = "Public. Requires a valid refresh token body; does not use Bearer auth."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tokens rotated"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody final RefreshTokenRequest request,
            final HttpServletRequest httpRequest
    ) {
        final AuthResponse response = authService.refreshToken(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @RequireAuthenticated
    @Operation(
            summary = "Logout current user, revoke refresh tokens, and record audit event",
            description = """
                    Requires authenticated JWT. Verifies principal (user, roles, permissions, tenant)
                    then revokes refresh tokens for the session.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logged out"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT"
            )
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @CurrentUser final AuthenticatedUser currentUser,
            @RequestBody(required = false) final LogoutRequest request,
            final HttpServletRequest httpRequest
    ) {
        authService.logout(
                request == null ? null : request.refreshToken(),
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @GetMapping("/profile")
    @RequireAuthenticated
    @Operation(
            summary = "Get the authenticated user's profile",
            description = """
                    Requires authenticated JWT. Returns the caller's own profile
                    (roles and effective permissions included for session bootstrap).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Tenant access denied"
            )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @CurrentUser final AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile retrieved successfully",
                authService.getCurrentUser()));
    }

    @PutMapping("/profile")
    @RequireAuthenticated
    @Operation(
            summary = "Update the authenticated user's profile",
            description = "Requires authenticated JWT. Updates only the caller's own profile fields."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile updated"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Tenant access denied"
            )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @CurrentUser final AuthenticatedUser currentUser,
            @Valid @RequestBody final UpdateProfileRequest request,
            final HttpServletRequest httpRequest
    ) {
        final UserProfileResponse response = authService.updateProfile(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PostMapping("/change-password")
    @RequireAuthenticated
    @Operation(
            summary = "Change the authenticated user's password",
            description = """
                    Requires authenticated JWT. Verifies the current password, then rotates
                    credentials and invalidates other refresh tokens.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password changed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing JWT or current password incorrect"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Tenant access denied"
            )
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @CurrentUser final AuthenticatedUser currentUser,
            @Valid @RequestBody final ChangePasswordRequest request,
            final HttpServletRequest httpRequest
    ) {
        authService.changePassword(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Request a password reset email for an account",
            description = "Public. Always returns a generic success message (no account enumeration)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Generic acknowledgement"
            )
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody final ForgotPasswordRequest request,
            final HttpServletRequest httpRequest
    ) {
        authService.forgotPassword(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success(
                "If an account exists for that email, a password reset link has been sent",
                null
        ));
    }

    @PostMapping("/reset-password")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Reset password using a single-use recovery token",
            description = "Public. Consumes a single-use reset token; does not reveal whether the token was valid beyond generic auth errors."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password reset"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired reset token"
            )
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody final ResetPasswordRequest request,
            final HttpServletRequest httpRequest
    ) {
        authService.resetPassword(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @PostMapping("/verify-email")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Verify account email using a single-use verification token",
            description = "Public. Activates the account (and tenant/hospital when applicable) after email verification."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email verified"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired verification token"
            )
    })
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody final VerifyEmailRequest request,
            final HttpServletRequest httpRequest
    ) {
        authService.verifyEmail(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/resend-verification")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Resend the email verification link for an unverified account",
            description = "Public. Always returns a generic success message (no account enumeration)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Generic acknowledgement"
            )
    })
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody final ResendVerificationRequest request,
            final HttpServletRequest httpRequest
    ) {
        authService.resendVerification(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success(
                "If an unverified account exists for that email, a verification link has been sent",
                null
        ));
    }

    /**
     * Authorization introspection endpoint for session bootstrap.
     */
    @GetMapping("/authorization")
    @RequireAuthenticated
    @Operation(
            summary = "Inspect the current principal's authorization context",
            description = """
                    Requires authenticated JWT. Returns the caller's userId, tenantId, roles,
                    and effective permissions for frontend route guards. Does not expose secrets.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authorization context resolved"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Tenant access denied"
            )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuthorizationContext(
            @CurrentUser final AuthenticatedUser currentUser
    ) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", currentUser.getUserId());
        payload.put("tenantId", currentUser.getTenantId());
        payload.put("email", currentUser.getEmail());
        payload.put("roles", currentUser.getRoles());
        payload.put("permissions", currentUser.getPermissions());
        return ResponseEntity.ok(ApiResponse.success("Authorization context resolved", payload));
    }

    /**
     * Permission-middleware probe: requires HOSPITAL_READ without exposing a business module.
     */
    @GetMapping("/authorization/hospital-access")
    @RequirePermission(PermissionConstants.HOSPITAL_READ)
    @Operation(
            summary = "Verify HOSPITAL_READ permission for the current principal",
            description = """
                    Requires authenticated JWT, matching tenant context, and permission HOSPITAL_READ.
                    Returns 403 with a generic access-denied body when the permission is missing.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hospital access permitted"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Missing HOSPITAL_READ or tenant access denied"
            )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyHospitalAccess(
            @CurrentUser final AuthenticatedUser currentUser
    ) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("allowed", true);
        payload.put("permission", PermissionConstants.HOSPITAL_READ);
        payload.put("userId", currentUser.getUserId());
        payload.put("tenantId", currentUser.getTenantId());
        return ResponseEntity.ok(ApiResponse.success("Hospital access permitted", payload));
    }
}
