package com.healthcare.hms.patients.immunization.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.patients.immunization.dto.request.UpsertImmunizationRequest;
import com.healthcare.hms.patients.immunization.dto.response.ImmunizationDueResponse;
import com.healthcare.hms.patients.immunization.dto.response.ImmunizationResponse;
import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import com.healthcare.hms.patients.immunization.service.ImmunizationService;
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
 * Patient immunization / vaccination API (Phase 5.5).
 */
@RestController
@RequestMapping("/api/v1/patients/{patientId}/immunizations")
@Tag(name = "Patient Immunizations", description = "Vaccination records with dose, lot, and next-due tracking")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class ImmunizationController {

    private final ImmunizationService immunizationService;

    public ImmunizationController(final ImmunizationService immunizationService) {
        this.immunizationService = immunizationService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(
            summary = "List patient immunizations",
            description = "Optional filter by ImmunizationStatus (ADMINISTERED, SCHEDULED, REFUSED, ENTERED_IN_ERROR)."
    )
    public ResponseEntity<ApiResponse<List<ImmunizationResponse>>> list(
            @PathVariable("patientId") final UUID patientId,
            @Parameter(description = "Filter by immunization status")
            @RequestParam(value = "status", required = false) final ImmunizationStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Immunizations retrieved successfully",
                immunizationService.list(patientId, status)
        ));
    }

    @GetMapping("/due")
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(
            summary = "Due / overdue immunizations",
            description = "ADMINISTERED records whose nextDueDate is on or before today."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Due immunizations",
                    content = @Content(schema = @Schema(implementation = ImmunizationDueResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<ApiResponse<ImmunizationDueResponse>> due(
            @PathVariable("patientId") final UUID patientId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Due immunizations retrieved successfully",
                immunizationService.getDue(patientId)
        ));
    }

    @GetMapping("/{immunizationId}")
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(summary = "Get immunization by id")
    public ResponseEntity<ApiResponse<ImmunizationResponse>> getById(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("immunizationId") final UUID immunizationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Immunization retrieved successfully",
                immunizationService.getById(patientId, immunizationId)
        ));
    }

    @PostMapping
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(summary = "Record immunization")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<ApiResponse<ImmunizationResponse>> create(
            @PathVariable("patientId") final UUID patientId,
            @Valid @RequestBody final UpsertImmunizationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final ImmunizationResponse response = immunizationService.create(
                patientId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Immunization created successfully", response));
    }

    @PutMapping("/{immunizationId}")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(summary = "Update immunization")
    public ResponseEntity<ApiResponse<ImmunizationResponse>> update(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("immunizationId") final UUID immunizationId,
            @Valid @RequestBody final UpsertImmunizationRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Immunization updated successfully",
                immunizationService.update(
                        patientId,
                        immunizationId,
                        request,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @DeleteMapping("/{immunizationId}")
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    @Operation(summary = "Soft-delete immunization", description = "Logical removal only — retained for audit.")
    public ResponseEntity<Void> delete(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("immunizationId") final UUID immunizationId,
            final HttpServletRequest httpRequest
    ) {
        immunizationService.softDelete(
                patientId,
                immunizationId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
