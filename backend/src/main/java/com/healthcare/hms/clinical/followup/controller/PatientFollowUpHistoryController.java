package com.healthcare.hms.clinical.followup.controller;

import com.healthcare.hms.clinical.enums.FollowUpStatus;
import com.healthcare.hms.clinical.followup.dto.response.FollowUpResponse;
import com.healthcare.hms.clinical.followup.service.FollowUpService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Patient follow-up history (cross-consultation planned return visits).
 */
@RestController
@RequestMapping("/api/v1/patients/{patientId}/follow-ups")
@Validated
@Tag(
        name = "Patient Follow-up History",
        description = "Paginated follow-up plans across consultations for care continuity review"
)
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class PatientFollowUpHistoryController {

    private final FollowUpService followUpService;

    public PatientFollowUpHistoryController(final FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "Patient follow-up history",
            description = """
                    Paginated list of follow-up plans for the patient, newest scheduledDate first.
                    Optional filters by status and scheduled date range. Cross-doctor visibility
                    scoped by patient (not current doctor).
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<FollowUpResponse>>> history(
            @PathVariable final UUID patientId,
            @Parameter(description = "Filter by follow-up status")
            @RequestParam(value = "status", required = false) final FollowUpStatus status,
            @Parameter(description = "Inclusive start date (scheduledDate)")
            @RequestParam(value = "fromDate", required = false) final LocalDate fromDate,
            @Parameter(description = "Inclusive end date (scheduledDate)")
            @RequestParam(value = "toDate", required = false) final LocalDate toDate,
            @PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.DESC)
            final Pageable pageable
    ) {
        final PageResponse<FollowUpResponse> page = followUpService.patientHistory(
                patientId,
                status,
                fromDate,
                toDate,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Patient follow-up history retrieved", page));
    }
}
