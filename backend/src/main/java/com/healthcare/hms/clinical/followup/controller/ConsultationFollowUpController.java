package com.healthcare.hms.clinical.followup.controller;

import com.healthcare.hms.clinical.followup.dto.request.CreateFollowUpRequest;
import com.healthcare.hms.clinical.followup.dto.request.UpdateFollowUpRequest;
import com.healthcare.hms.clinical.followup.dto.request.UpdateFollowUpStatusRequest;
import com.healthcare.hms.clinical.followup.dto.response.FollowUpResponse;
import com.healthcare.hms.clinical.followup.service.FollowUpService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultation-scoped follow-up planning API (Phase 7.4 / 7.7).
 */
@RestController
@RequestMapping("/api/v1/consultations/{consultationId}/follow-ups")
@Validated
@Tag(
        name = "Consultation Follow-ups",
        description = "Planned return visits with priority, clinical recommendations, status lifecycle, and reminder readiness"
)
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class ConsultationFollowUpController {

    private final FollowUpService followUpService;

    public ConsultationFollowUpController(final FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Plan follow-up",
            description = """
                    Creates a follow-up plan for the consultation. Supports reason, clinical
                    recommendations, priority, doctor, reminder lead days, and optional appointment link.
                    Scheduled date must not be in the past. Writes require an editable consultation.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Follow-up created",
                    content = @Content(schema = @Schema(implementation = FollowUpResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Consultation not found")
    })
    public ResponseEntity<ApiResponse<FollowUpResponse>> create(
            @PathVariable final UUID consultationId,
            @Valid @RequestBody final CreateFollowUpRequest request,
            final HttpServletRequest httpRequest
    ) {
        final FollowUpResponse response = followUpService.create(
                consultationId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Follow-up created successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "List consultation follow-ups",
            description = "Returns all follow-up plans for the consultation ordered by scheduledDate ascending."
    )
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> list(
            @PathVariable final UUID consultationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Follow-ups retrieved successfully",
                followUpService.listByConsultation(consultationId)
        ));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "Get follow-up", description = "Returns a single follow-up plan. View is audit-logged.")
    public ResponseEntity<ApiResponse<FollowUpResponse>> getById(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final FollowUpResponse response = followUpService.getById(
                consultationId,
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Follow-up retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Update follow-up",
            description = """
                    Updates plan fields (date, priority, recommendations, reminders) while the consultation
                    is editable or the follow-up remains open. Status changes are validated by transition rules.
                    """
    )
    public ResponseEntity<ApiResponse<FollowUpResponse>> update(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            @Valid @RequestBody final UpdateFollowUpRequest request,
            final HttpServletRequest httpRequest
    ) {
        final FollowUpResponse response = followUpService.update(
                consultationId,
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Follow-up updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Update follow-up status",
            description = """
                    Transitions status (PENDING ↔ SCHEDULED → COMPLETED / CANCELLED / MISSED).
                    Allowed after the consultation is completed — for closing the care loop.
                    """
    )
    public ResponseEntity<ApiResponse<FollowUpResponse>> updateStatus(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            @Valid @RequestBody final UpdateFollowUpStatusRequest request,
            final HttpServletRequest httpRequest
    ) {
        final FollowUpResponse response = followUpService.updateStatus(
                consultationId,
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Follow-up status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    @Operation(
            summary = "Soft-delete follow-up",
            description = "Marks a follow-up plan as void while the consultation remains editable."
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        followUpService.delete(
                consultationId,
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Follow-up deleted successfully", null));
    }
}
