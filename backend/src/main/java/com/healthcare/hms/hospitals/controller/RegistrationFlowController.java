package com.healthcare.hms.hospitals.controller;

import com.healthcare.hms.auth.dto.request.PreRegisterAdminRequest;
import com.healthcare.hms.auth.dto.response.PreRegisterAdminResponse;
import com.healthcare.hms.auth.service.RegistrationSetupService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.hospitals.dto.request.CompleteRegistrationRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.hospitals.service.HospitalRegistrationService;
import com.healthcare.hms.security.annotation.PublicEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Two-step hospital onboarding: admin account (step 1) then hospital setup (step 2).
 *
 * <p>Step 1 validates admin data and returns a registration token (no DB writes).
 * Step 2 consumes the token and atomically creates the tenant, hospital, roles,
 * and admin user. Abandoned tokens expire after 1 hour.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Registration Flow", description = "Multi-step hospital onboarding (Phase 2)")
public class RegistrationFlowController {

    private final RegistrationSetupService registrationSetupService;
    private final HospitalRegistrationService hospitalRegistrationService;

    public RegistrationFlowController(
            final RegistrationSetupService registrationSetupService,
            final HospitalRegistrationService hospitalRegistrationService
    ) {
        this.registrationSetupService = registrationSetupService;
        this.hospitalRegistrationService = hospitalRegistrationService;
    }

    @PostMapping("/auth/register-admin")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Step 1 — Register admin account",
            description = """
                    Creates a temporary registration setup for the admin. No DB records are created;
                    a token is returned that must be presented in the step 2 endpoint to complete
                    hospital + tenant creation. The token expires after 1 hour.
                    """
    )
    public ResponseEntity<ApiResponse<PreRegisterAdminResponse>> registerAdmin(
            @Valid @RequestBody final PreRegisterAdminRequest request
    ) {
        final RegistrationSetupService.SetupData data = new RegistrationSetupService.SetupData(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password(),
                request.phone()
        );
        final String token = registrationSetupService.createSetup(data);

        final PreRegisterAdminResponse response = new PreRegisterAdminResponse(
                token,
                request.email(),
                60
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Admin data validated. Proceed to hospital setup.", response));
    }

    @PostMapping("/hospitals/complete-registration")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Step 2 — Complete hospital registration",
            description = """
                    Consumes the registration token from step 1 and atomically creates the tenant,
                    default hospital, default roles with permission grants, and the initial hospital
                    administrator. If any step fails, the entire registration is rolled back.
                    """
    )
    public ResponseEntity<ApiResponse<HospitalRegistrationResponse>> completeRegistration(
            @Valid @RequestBody final CompleteRegistrationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final RegistrationSetupService.SetupData setupData =
                registrationSetupService.consumeSetup(request.registrationToken());

        if (setupData == null) {
            throw new BusinessException(
                    "INVALID_REGISTRATION_TOKEN",
                    "Registration token is invalid, expired, or has already been used. Please restart the registration process."
            );
        }

        // Build the full registration request from the token data + step 2 input
        final com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest fullRequest =
                new com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest(
                        request.hospitalName(),
                        request.hospitalEmail(),
                        request.hospitalPhone(),
                        request.hospitalAddress(),
                        request.subscriptionPlan(),
                        setupData.firstName(),
                        setupData.lastName(),
                        setupData.email(),
                        setupData.password(),
                        setupData.phone()
                );

        final HospitalRegistrationResponse response = hospitalRegistrationService.register(
                fullRequest,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Hospital registered successfully. Please verify the administrator email before signing in",
                        response
                ));
    }
}
