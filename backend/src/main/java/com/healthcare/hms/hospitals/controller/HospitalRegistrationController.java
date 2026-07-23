package com.healthcare.hms.hospitals.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.hospitals.service.HospitalRegistrationService;
import com.healthcare.hms.security.annotation.PublicEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * Public hospital onboarding API (Phase 2.5). Settings live under {@code /settings} (Phase 2.6).
 */
@RestController
@RequestMapping("/api/v1/hospitals")
@Tag(name = "Hospital Registration", description = "Atomic hospital tenant onboarding")
public class HospitalRegistrationController {

    private final HospitalRegistrationService hospitalRegistrationService;

    public HospitalRegistrationController(final HospitalRegistrationService hospitalRegistrationService) {
        this.hospitalRegistrationService = hospitalRegistrationService;
    }

    @PostMapping("/register")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Register a hospital",
            description = """
                    Public (anonymous). Atomically creates a tenant, default hospital profile,
                    default roles with permission grants, and the initial hospital administrator.
                    If any step fails, the entire registration is rolled back. The administrator
                    must verify email before signing in; the tenant remains PENDING until verification.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Hospital registered successfully",
                    content = @Content(schema = @Schema(implementation = HospitalRegistrationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Hospital or administrator email already exists"
            )
    })
    public ResponseEntity<ApiResponse<HospitalRegistrationResponse>> register(
            @Valid @RequestBody final HospitalRegistrationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final HospitalRegistrationResponse response = hospitalRegistrationService.register(
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
}
