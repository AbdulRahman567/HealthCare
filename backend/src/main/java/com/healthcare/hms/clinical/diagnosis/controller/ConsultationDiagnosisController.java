package com.healthcare.hms.clinical.diagnosis.controller;

import com.healthcare.hms.clinical.diagnosis.dto.request.CreateDiagnosisRequest;
import com.healthcare.hms.clinical.diagnosis.dto.request.UpdateDiagnosisRequest;
import com.healthcare.hms.clinical.diagnosis.dto.response.DiagnosisResponse;
import com.healthcare.hms.clinical.diagnosis.service.DiagnosisService;
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
 * Consultation-scoped diagnosis API (Phase 7.4).
 *
 * <p>Structured encounter diagnoses with ICD-10 coding, type, status, and severity.
 * Supports multiple diagnoses per consultation with at most one primary.
 */
@RestController
@RequestMapping("/api/v1/consultations/{consultationId}/diagnoses")
@Validated
@Tag(
        name = "Consultation Diagnoses",
        description = "Primary, secondary, and working diagnoses with ICD-10 coding and clinical status"
)
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class ConsultationDiagnosisController {

    private final DiagnosisService diagnosisService;

    public ConsultationDiagnosisController(final DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Add diagnosis",
            description = """
                    Appends a structured diagnosis row to the consultation. At most one PRIMARY
                    diagnosis is allowed per consultation. Sequence number is auto-assigned when
                    omitted. Writes are allowed only while the consultation is editable.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Diagnosis created",
                    content = @Content(schema = @Schema(implementation = DiagnosisResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Consultation not found")
    })
    public ResponseEntity<ApiResponse<DiagnosisResponse>> create(
            @PathVariable final UUID consultationId,
            @Valid @RequestBody final CreateDiagnosisRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DiagnosisResponse response = diagnosisService.create(
                consultationId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Diagnosis created successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "List consultation diagnoses",
            description = "Returns all diagnoses for the consultation ordered by sequenceNumber ascending."
    )
    public ResponseEntity<ApiResponse<List<DiagnosisResponse>>> list(
            @PathVariable final UUID consultationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Diagnoses retrieved successfully",
                diagnosisService.listByConsultation(consultationId)
        ));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "Get diagnosis", description = "Returns a single diagnosis row. View is audit-logged.")
    public ResponseEntity<ApiResponse<DiagnosisResponse>> getById(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final DiagnosisResponse response = diagnosisService.getById(
                consultationId,
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Diagnosis retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Update diagnosis",
            description = "Corrects or updates a diagnosis while the parent consultation remains editable."
    )
    public ResponseEntity<ApiResponse<DiagnosisResponse>> update(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            @Valid @RequestBody final UpdateDiagnosisRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DiagnosisResponse response = diagnosisService.update(
                consultationId,
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Diagnosis updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    @Operation(
            summary = "Soft-delete diagnosis",
            description = "Marks a diagnosis as entered-in-error while the consultation remains editable."
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        diagnosisService.delete(
                consultationId,
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Diagnosis deleted successfully", null));
    }
}
