package com.healthcare.hms.clinical.vitals.controller;

import com.healthcare.hms.clinical.vitals.dto.request.RecordVitalSignsRequest;
import com.healthcare.hms.clinical.vitals.dto.request.UpdateVitalSignsRequest;
import com.healthcare.hms.clinical.vitals.dto.response.VitalSignsResponse;
import com.healthcare.hms.clinical.vitals.service.VitalSignsService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
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
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultation-scoped vital-signs API (Phase 7.3).
 *
 * <p>Each POST appends a measurement row — full history is retained for trend analysis.
 */
@RestController
@RequestMapping("/api/v1/consultations/{consultationId}/vital-signs")
@Validated
@Tag(name = "Consultation Vital Signs", description = "Temperature, heart rate, BP, SpO2, height, weight, BMI, pain scale")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class ConsultationVitalSignsController {

    private final VitalSignsService vitalSignsService;

    public ConsultationVitalSignsController(final VitalSignsService vitalSignsService) {
        this.vitalSignsService = vitalSignsService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Record vital signs",
            description = """
                    Appends a new vital-signs measurement for the consultation. BMI is computed
                    server-side when height and weight are supplied. Blood pressure requires both
                    systolic and diastolic values. At least one measurement is required.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Vital signs recorded",
                    content = @Content(schema = @Schema(implementation = VitalSignsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Consultation not found")
    })
    public ResponseEntity<ApiResponse<VitalSignsResponse>> record(
            @PathVariable final UUID consultationId,
            @Valid @RequestBody final RecordVitalSignsRequest request,
            final HttpServletRequest httpRequest
    ) {
        final VitalSignsResponse response = vitalSignsService.record(
                consultationId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vital signs recorded successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "List consultation vital signs",
            description = "Returns all measurement rows for the consultation ordered by recordedAt ascending."
    )
    public ResponseEntity<ApiResponse<List<VitalSignsResponse>>> list(
            @PathVariable final UUID consultationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vital signs retrieved successfully",
                vitalSignsService.listByConsultation(consultationId)
        ));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "Get vital signs measurement", description = "Returns a single measurement row. View is audit-logged.")
    public ResponseEntity<ApiResponse<VitalSignsResponse>> getById(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final VitalSignsResponse response = vitalSignsService.getById(
                consultationId,
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Vital signs retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Correct vital signs measurement",
            description = "Updates a row entered in error while the parent consultation remains editable."
    )
    public ResponseEntity<ApiResponse<VitalSignsResponse>> update(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            @Valid @RequestBody final UpdateVitalSignsRequest request,
            final HttpServletRequest httpRequest
    ) {
        final VitalSignsResponse response = vitalSignsService.update(
                consultationId,
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Vital signs updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    @Operation(
            summary = "Soft-delete vital signs measurement",
            description = "Marks a row as entered-in-error while the consultation remains editable."
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        vitalSignsService.delete(
                consultationId,
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Vital signs deleted successfully", null));
    }
}
