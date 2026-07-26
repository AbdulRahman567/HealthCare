package com.healthcare.hms.patients.allergy.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.patients.allergy.dto.request.UpsertAllergyRequest;
import com.healthcare.hms.patients.allergy.dto.response.AllergyBannerResponse;
import com.healthcare.hms.patients.allergy.dto.response.AllergyCriticalAlertResponse;
import com.healthcare.hms.patients.allergy.dto.response.AllergyResponse;
import com.healthcare.hms.patients.allergy.enums.AllergyType;
import com.healthcare.hms.patients.allergy.service.AllergyService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Safety-critical patient allergy API (Phase 5.4).
 *
 * <p>Banner and critical-alert endpoints exist so allergies cannot be buried
 * in a general profile tab (healthcare-domain requirement).
 */
@RestController
@RequestMapping("/api/v1/patients/{patientId}/allergies")
@Tag(name = "Patient Allergies", description = "Drug, food, and environmental allergies with clinical alert surfaces")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class AllergyController {

    private final AllergyService allergyService;

    public AllergyController(final AllergyService allergyService) {
        this.allergyService = allergyService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(summary = "List patient allergies", description = "Optional filter by AllergyType (DRUG, FOOD, ENVIRONMENTAL, OTHER).")
    public ResponseEntity<ApiResponse<List<AllergyResponse>>> list(
            @PathVariable("patientId") final UUID patientId,
            @Parameter(description = "Filter by allergy type")
            @RequestParam(value = "type", required = false) final AllergyType type
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Allergies retrieved successfully",
                allergyService.list(patientId, type)
        ));
    }

    @GetMapping("/banner")
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(
            summary = "Patient banner allergy alerts",
            description = """
                    Returns allergies flagged for the chart banner, critical-alert count,
                    and whether active drug allergies exist (NKDA inference when none).
                    Call this when opening any patient chart.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Banner alerts",
                    content = @Content(schema = @Schema(implementation = AllergyBannerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<ApiResponse<AllergyBannerResponse>> banner(
            @PathVariable("patientId") final UUID patientId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Allergy banner alerts retrieved successfully",
                allergyService.getBannerAlerts(patientId)
        ));
    }

    @GetMapping("/critical")
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(
            summary = "Critical allergy alerts",
            description = "Active allergies with criticalAlert=true (includes life-threatening / anaphylaxis)."
    )
    public ResponseEntity<ApiResponse<AllergyCriticalAlertResponse>> critical(
            @PathVariable("patientId") final UUID patientId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Critical allergy alerts retrieved successfully",
                allergyService.getCriticalAlerts(patientId)
        ));
    }

    @GetMapping("/{allergyId}")
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(summary = "Get allergy by id")
    public ResponseEntity<ApiResponse<AllergyResponse>> getById(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("allergyId") final UUID allergyId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Allergy retrieved successfully",
                allergyService.getById(patientId, allergyId)
        ));
    }

    @PostMapping
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(
            summary = "Add allergy",
            description = "LIFE_THREATENING severity or ANAPHYLAXIS reaction auto-sets criticalAlert and showOnBanner."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<ApiResponse<AllergyResponse>> create(
            @PathVariable("patientId") final UUID patientId,
            @Valid @RequestBody final UpsertAllergyRequest request,
            final HttpServletRequest httpRequest
    ) {
        final AllergyResponse response = allergyService.create(
                patientId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Allergy created successfully", response));
    }

    @PutMapping("/{allergyId}")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(summary = "Update allergy")
    public ResponseEntity<ApiResponse<AllergyResponse>> update(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("allergyId") final UUID allergyId,
            @Valid @RequestBody final UpsertAllergyRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Allergy updated successfully",
                allergyService.update(
                        patientId,
                        allergyId,
                        request,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @DeleteMapping("/{allergyId}")
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    @Operation(summary = "Soft-delete allergy", description = "Logical removal only — retained for audit.")
    public ResponseEntity<Void> delete(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("allergyId") final UUID allergyId,
            final HttpServletRequest httpRequest
    ) {
        allergyService.softDelete(
                patientId,
                allergyId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
