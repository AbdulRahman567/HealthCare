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
import com.healthcare.hms.security.annotation.RequiresPermission;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication and session APIs with method-level authorization annotations.
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
    @Operation(summary = "Authenticate user and issue access plus refresh tokens")
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
    @Operation(
            summary = "Register a new hospital tenant (compatibility)",
            description = "Delegates to atomic hospital registration. Prefer POST /api/v1/hospitals/register."
    )
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
    @Operation(
            summary = "Legacy initial admin registration (disabled)",
            description = """
                    Disabled in Phase 2.7 for tenant security. Prefer atomic
                    POST /api/v1/hospitals/register which creates tenant + admin together.
                    """
    )
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
    @Operation(summary = "Rotate refresh token and issue a new access token")
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
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout current user, revoke refresh tokens, and record audit event")
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
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @CurrentUser final AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile retrieved successfully",
                authService.getCurrentUser()));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update the authenticated user's profile")
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
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change the authenticated user's password")
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
    @Operation(summary = "Request a password reset email for an account")
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
    @Operation(summary = "Reset password using a single-use recovery token")
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
    @Operation(summary = "Verify account email using a single-use verification token")
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
    @Operation(summary = "Resend the email verification link for an unverified account")
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
     * Requires authentication; available to all clinical staff roles.
     */
    @GetMapping("/authorization")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Inspect the current principal's authorization context")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuthorizationContext(
            @CurrentUser final AuthenticatedUser currentUser
    ) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", currentUser.getUserId());
        payload.put("tenantId", currentUser.getTenantId());
        payload.put("email", currentUser.getEmail());
        payload.put("roles", currentUser.getRoles());
        payload.put("permissions", currentUser.getPermissions());
        payload.put("samplePermission", PermissionConstants.USER_READ);
        return ResponseEntity.ok(ApiResponse.success("Authorization context resolved", payload));
    }

    /**
     * Permission-middleware probe: requires HOSPITAL_READ without exposing a business module.
     */
    @GetMapping("/authorization/hospital-access")
    @PreAuthorize("isAuthenticated()")
    @RequiresPermission(PermissionConstants.HOSPITAL_READ)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Verify HOSPITAL_READ permission for the current principal")
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
