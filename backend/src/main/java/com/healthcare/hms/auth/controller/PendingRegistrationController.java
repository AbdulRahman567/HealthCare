package com.healthcare.hms.auth.controller;

import com.healthcare.hms.auth.dto.request.RegistrationRequest;
import com.healthcare.hms.auth.dto.request.ResendVerificationRequest;
import com.healthcare.hms.auth.dto.response.PendingRegistrationResponse;
import com.healthcare.hms.auth.service.PendingRegistrationService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.security.annotation.PublicEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 7 registration: captures the full signup payload as a pending record, sends a
 * verification email, and only creates the real tenant/hospital/admin records when the
 * emailed link is clicked. No real account data exists before verification.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Registration (pending)", description = "Email-gated hospital onboarding (Phase 7)")
public class PendingRegistrationController {

    private final PendingRegistrationService pendingRegistrationService;

    public PendingRegistrationController(final PendingRegistrationService pendingRegistrationService) {
        this.pendingRegistrationService = pendingRegistrationService;
    }

    @PostMapping("/register")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Submit single-page registration",
            description = "Stores only a lightweight pending registration and emails a verification link. No tenant, hospital, or admin account is created yet."
    )
    public ResponseEntity<ApiResponse<PendingRegistrationResponse>> submit(
            @Valid @RequestBody final RegistrationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PendingRegistrationResponse response = pendingRegistrationService.submit(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(
                        "Registration received. Verify your email to create your hospital account.",
                        response
                ));
    }

    @PostMapping("/register/resend-verification")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(summary = "Resend registration verification email")
    public ResponseEntity<ApiResponse<PendingRegistrationResponse>> resendVerification(
            @Valid @RequestBody final ResendVerificationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PendingRegistrationResponse response = pendingRegistrationService.resendVerification(
                request.email(),
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Verification email resent.", response));
    }

    @GetMapping("/verify-registration")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Complete registration from verification link",
            description = "Consumes the pending-registration token from the email and creates the real tenant, hospital, roles, and admin account. trialEndsAt starts from now."
    )
    public ResponseEntity<ApiResponse<HospitalRegistrationResponse>> verify(
            @RequestParam("token") final String token,
            final HttpServletRequest httpRequest
    ) {
        final HospitalRegistrationResponse response = pendingRegistrationService.verify(
                token,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Registration complete. Your hospital account is ready.",
                        response
                ));
    }
}
