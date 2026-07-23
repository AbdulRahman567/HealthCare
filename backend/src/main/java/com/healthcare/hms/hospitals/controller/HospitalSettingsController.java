package com.healthcare.hms.hospitals.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.hospitals.dto.request.UpdateHospitalSettingsRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalSettingsResponse;
import com.healthcare.hms.hospitals.service.HospitalSettingsService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenant-scoped hospital settings API (Phase 2.6).
 */
@RestController
@RequestMapping("/api/v1/hospitals/settings")
@Tag(name = "Hospital Settings", description = "Tenant-isolated hospital profile and operational settings")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class HospitalSettingsController {

    private final HospitalSettingsService hospitalSettingsService;

    public HospitalSettingsController(final HospitalSettingsService hospitalSettingsService) {
        this.hospitalSettingsService = hospitalSettingsService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.HOSPITAL_READ)
    @Operation(
            summary = "Get hospital settings",
            description = """
                    Requires JWT (Bearer), matching tenant context, role membership, and
                    permission HOSPITAL_READ. Returns the current tenant's default hospital
                    settings: profile, logo, timezone, currency, language, contact information,
                    address, and working hours.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Settings retrieved",
                    content = @Content(schema = @Schema(implementation = HospitalSettingsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Missing HOSPITAL_READ permission or tenant access denied"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Default hospital not found for tenant"
            )
    })
    public ResponseEntity<ApiResponse<HospitalSettingsResponse>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(
                "Hospital settings retrieved successfully",
                hospitalSettingsService.getSettings()
        ));
    }

    @PutMapping
    @RequirePermission(PermissionConstants.HOSPITAL_UPDATE)
    @Operation(
            summary = "Update hospital settings",
            description = """
                    Requires JWT (Bearer), matching tenant context, role membership, and
                    permission HOSPITAL_UPDATE. Replaces the current tenant's default hospital
                    settings. Changes are audited. Client-supplied hospital or tenant
                    identifiers are not accepted.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Settings updated",
                    content = @Content(schema = @Schema(implementation = HospitalSettingsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Missing HOSPITAL_UPDATE permission or tenant access denied"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Default hospital not found for tenant"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Hospital name already exists for tenant"
            )
    })
    public ResponseEntity<ApiResponse<HospitalSettingsResponse>> updateSettings(
            @Valid @RequestBody final UpdateHospitalSettingsRequest request,
            final HttpServletRequest httpRequest
    ) {
        final HospitalSettingsResponse response = hospitalSettingsService.updateSettings(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Hospital settings updated successfully", response));
    }
}
