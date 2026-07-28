package com.healthcare.hms.prescriptions.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.prescriptions.dto.request.CancelPrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.request.CreatePrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.request.PrescriptionSearchCriteria;
import com.healthcare.hms.prescriptions.dto.request.UpdatePrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionResponse;
import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
import com.healthcare.hms.prescriptions.service.PrescriptionService;
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
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Digital prescription APIs (Phase 7.5).
 */
@RestController
@RequestMapping("/api/v1/prescriptions")
@Validated
@Tag(
        name = "Prescriptions",
        description = "Digital prescriptions with medicine lines, dosage, frequency, route, duration, quantity, and refills"
)
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(final PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.PRESCRIPTION_CREATE)
    @Operation(
            summary = "Create prescription",
            description = """
                    Creates a prescription linked to a consultation with one or more medicine lines.
                    Duplicate medicines (case-insensitive) are rejected. Optional issueImmediately
                    transitions DRAFT → ISSUED in the same request.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Prescription created",
                    content = @Content(schema = @Schema(implementation = PrescriptionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Consultation not found")
    })
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(
            @Valid @RequestBody final CreatePrescriptionRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PrescriptionResponse response = prescriptionService.create(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Prescription created successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.PRESCRIPTION_READ)
    @Operation(summary = "Search prescriptions", description = "Paginated prescription search with optional filters.")
    public ResponseEntity<ApiResponse<PageResponse<PrescriptionResponse>>> search(
            @RequestParam(value = "prescriptionNumber", required = false) final String prescriptionNumber,
            @RequestParam(value = "patientId", required = false) final UUID patientId,
            @RequestParam(value = "doctorId", required = false) final UUID doctorId,
            @RequestParam(value = "consultationId", required = false) final UUID consultationId,
            @RequestParam(value = "status", required = false) final PrescriptionStatus status,
            @RequestParam(value = "fromDate", required = false) final LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) final LocalDate toDate,
            @PageableDefault(size = 20, sort = "prescriptionDate", direction = Sort.Direction.DESC)
            final Pageable pageable
    ) {
        final PageResponse<PrescriptionResponse> page = prescriptionService.search(
                new PrescriptionSearchCriteria(
                        prescriptionNumber, patientId, doctorId, consultationId, status, fromDate, toDate),
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Prescriptions retrieved successfully", page));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.PRESCRIPTION_READ)
    @Operation(summary = "Get prescription", description = "Returns a prescription with medicine lines. View is audit-logged.")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getById(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final PrescriptionResponse response = prescriptionService.getById(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Prescription retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.PRESCRIPTION_UPDATE)
    @Operation(
            summary = "Update prescription",
            description = "Updates a DRAFT prescription. When items are supplied, the full line set is replaced."
    )
    public ResponseEntity<ApiResponse<PrescriptionResponse>> update(
            @PathVariable final UUID id,
            @Valid @RequestBody final UpdatePrescriptionRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PrescriptionResponse response = prescriptionService.update(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Prescription updated successfully", response));
    }

    @PatchMapping("/{id}/issue")
    @RequirePermission(PermissionConstants.PRESCRIPTION_UPDATE)
    @Operation(summary = "Issue prescription", description = "Transitions DRAFT → ISSUED. Requires at least one medicine line.")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> issue(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final PrescriptionResponse response = prescriptionService.issue(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Prescription issued successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @RequirePermission(PermissionConstants.PRESCRIPTION_UPDATE)
    @Operation(
            summary = "Cancel prescription",
            description = "Cancels a DRAFT, ISSUED, or PARTIALLY_DISPENSED prescription. Dispensed prescriptions cannot be cancelled."
    )
    public ResponseEntity<ApiResponse<PrescriptionResponse>> cancel(
            @PathVariable final UUID id,
            @Valid @RequestBody(required = false) final CancelPrescriptionRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PrescriptionResponse response = prescriptionService.cancel(
                id,
                request == null ? new CancelPrescriptionRequest(null) : request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Prescription cancelled successfully", response));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionConstants.PRESCRIPTION_DELETE)
    @Operation(summary = "Soft-delete prescription", description = "Soft-deletes a DRAFT prescription and its medicine lines.")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        prescriptionService.delete(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Prescription deleted successfully", null));
    }
}
