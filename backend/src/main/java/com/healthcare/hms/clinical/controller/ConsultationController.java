package com.healthcare.hms.clinical.controller;

import com.healthcare.hms.clinical.dto.request.CompleteConsultationRequest;
import com.healthcare.hms.clinical.dto.request.ConsultationSearchCriteria;
import com.healthcare.hms.clinical.dto.request.CreateConsultationRequest;
import com.healthcare.hms.clinical.dto.request.UpdateConsultationDocumentationRequest;
import com.healthcare.hms.clinical.dto.response.ClinicalSummaryResponse;
import com.healthcare.hms.clinical.dto.response.ConsultationResponse;
import com.healthcare.hms.clinical.enums.ConsultationStatus;
import com.healthcare.hms.clinical.service.ConsultationService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
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
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
 * Consultation management API (Phase 7.2).
 *
 * <p>Lifecycle: create (DRAFT) → start → pause/resume → complete.
 * Clinical documentation: chief complaint, HPI, physical examination, doctor notes, summary, advice.
 */
@RestController
@RequestMapping("/api/v1/consultations")
@Validated
@Tag(name = "Consultations", description = "Clinical consultation lifecycle and documentation")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(final ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.VISIT_CREATE)
    @Operation(
            summary = "Create consultation",
            description = """
                    Creates a DRAFT consultation for an ACTIVE patient with an ACTIVE doctor.
                    Optionally links a SCHEDULED or CONFIRMED appointment. Set startImmediately=true
                    to transition to IN_PROGRESS in the same request.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Consultation created",
                    content = @Content(schema = @Schema(implementation = ConsultationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient/doctor/department not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Appointment already has a consultation")
    })
    public ResponseEntity<ApiResponse<ConsultationResponse>> create(
            @Valid @RequestBody final CreateConsultationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final ConsultationResponse response = consultationService.create(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consultation created successfully", response));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "Get consultation", description = "Returns consultation detail including clinical summary. View is audit-logged.")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getById(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final ConsultationResponse response = consultationService.getById(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Consultation retrieved", response));
    }

    @GetMapping("/{id}/clinical-summary")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "Get clinical summary",
            description = "Returns chief complaint, HPI, physical examination, doctor notes, summary, and advice."
    )
    public ResponseEntity<ApiResponse<ClinicalSummaryResponse>> getClinicalSummary(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final ClinicalSummaryResponse response = consultationService.getClinicalSummary(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Clinical summary retrieved", response));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "Search consultations",
            description = """
                    Paginated consultation directory with database-level filters.
                    Doctor-scoped actors are limited to their own consultations.
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<ConsultationResponse>>> search(
            @Parameter(description = "Consultation number prefix (case-insensitive)")
            @RequestParam(value = "consultationNumber", required = false)
            @Size(max = 100) final String consultationNumber,
            @RequestParam(value = "patientId", required = false) final UUID patientId,
            @Parameter(description = "Patient name or MRN (contains)")
            @RequestParam(value = "patientName", required = false)
            @Size(max = 100) final String patientName,
            @RequestParam(value = "doctorId", required = false) final UUID doctorId,
            @Parameter(description = "Doctor name or employee code")
            @RequestParam(value = "doctorName", required = false)
            @Size(max = 100) final String doctorName,
            @RequestParam(value = "departmentId", required = false) final UUID departmentId,
            @RequestParam(value = "status", required = false) final ConsultationStatus status,
            @RequestParam(value = "fromDate", required = false) final LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) final LocalDate toDate,
            @RequestParam(value = "appointmentId", required = false) final UUID appointmentId,
            @PageableDefault(size = 20, sort = "consultationDate", direction = Sort.Direction.DESC)
            final Pageable pageable
    ) {
        final ConsultationSearchCriteria criteria = new ConsultationSearchCriteria(
                consultationNumber,
                patientId,
                patientName,
                doctorId,
                doctorName,
                departmentId,
                status,
                fromDate,
                toDate,
                appointmentId
        );
        final PageResponse<ConsultationResponse> page = consultationService.search(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Consultations retrieved", page));
    }

    @PutMapping("/{id}/documentation")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Update clinical documentation",
            description = """
                    Updates chief complaint, history of present illness, physical examination,
                    doctor notes, clinical summary, and advice while the consultation is editable.
                    """
    )
    public ResponseEntity<ApiResponse<ConsultationResponse>> updateDocumentation(
            @PathVariable final UUID id,
            @Valid @RequestBody final UpdateConsultationDocumentationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final ConsultationResponse response = consultationService.updateDocumentation(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Consultation documentation updated", response));
    }

    @PatchMapping("/{id}/start")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(summary = "Start consultation", description = "Transitions DRAFT → IN_PROGRESS. Only one active consultation per doctor.")
    public ResponseEntity<ApiResponse<ConsultationResponse>> start(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final ConsultationResponse response = consultationService.start(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Consultation started", response));
    }

    @PatchMapping("/{id}/pause")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(summary = "Pause consultation", description = "Transitions IN_PROGRESS → PAUSED.")
    public ResponseEntity<ApiResponse<ConsultationResponse>> pause(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final ConsultationResponse response = consultationService.pause(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Consultation paused", response));
    }

    @PatchMapping("/{id}/resume")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(summary = "Resume consultation", description = "Transitions PAUSED → IN_PROGRESS.")
    public ResponseEntity<ApiResponse<ConsultationResponse>> resume(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final ConsultationResponse response = consultationService.resume(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Consultation resumed", response));
    }

    @PatchMapping("/{id}/complete")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Complete consultation",
            description = """
                    Closes the encounter (IN_PROGRESS or PAUSED → COMPLETED).
                    Optionally sets final summary and advice. Completes linked appointment and
                    IN_CONSULTATION queue entry when applicable.
                    """
    )
    public ResponseEntity<ApiResponse<ConsultationResponse>> complete(
            @PathVariable final UUID id,
            @Valid @RequestBody(required = false) final CompleteConsultationRequest request,
            final HttpServletRequest httpRequest
    ) {
        final ConsultationResponse response = consultationService.complete(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Consultation completed", response));
    }

    @PatchMapping("/{id}/cancel")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Cancel consultation",
            description = """
                    Cancels an editable consultation (DRAFT / IN_PROGRESS / PAUSED → CANCELLED).
                    Releases the linked IN_CONSULTATION queue entry when present; does not
                    complete the appointment.
                    """
    )
    public ResponseEntity<ApiResponse<ConsultationResponse>> cancel(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final ConsultationResponse response = consultationService.cancel(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Consultation cancelled", response));
    }
}
