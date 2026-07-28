package com.healthcare.hms.clinical.followup.controller;

import com.healthcare.hms.clinical.enums.FollowUpPriority;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
import com.healthcare.hms.clinical.followup.dto.request.FollowUpSearchCriteria;
import com.healthcare.hms.clinical.followup.dto.response.FollowUpResponse;
import com.healthcare.hms.clinical.followup.service.FollowUpService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
 * Optimized follow-up search and due-list APIs (Phase 7.7).
 */
@RestController
@RequestMapping("/api/v1/follow-ups")
@Validated
@Tag(
        name = "Follow-ups",
        description = "Tenant-wide follow-up search, due lists, and direct lookup"
)
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class FollowUpController {

    private final FollowUpService followUpService;

    public FollowUpController(final FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "Search follow-ups",
            description = """
                    Paginated follow-up search. Doctor-scoped actors are limited to their own plans.
                    Use overdueOnly / dueSoonOnly for care-gap and reminder workflows.
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<FollowUpResponse>>> search(
            @RequestParam(value = "patientId", required = false) final UUID patientId,
            @RequestParam(value = "doctorId", required = false) final UUID doctorId,
            @RequestParam(value = "consultationId", required = false) final UUID consultationId,
            @RequestParam(value = "status", required = false) final FollowUpStatus status,
            @RequestParam(value = "priority", required = false) final FollowUpPriority priority,
            @RequestParam(value = "fromDate", required = false) final LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) final LocalDate toDate,
            @Parameter(description = "Only open follow-ups with scheduledDate before today")
            @RequestParam(value = "overdueOnly", required = false) final Boolean overdueOnly,
            @Parameter(description = "Only open follow-ups due within dueWithinDays (default 7)")
            @RequestParam(value = "dueSoonOnly", required = false) final Boolean dueSoonOnly,
            @RequestParam(value = "dueWithinDays", required = false) final Integer dueWithinDays,
            @PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        final PageResponse<FollowUpResponse> page = followUpService.search(
                new FollowUpSearchCriteria(
                        patientId,
                        doctorId,
                        consultationId,
                        status,
                        priority,
                        fromDate,
                        toDate,
                        overdueOnly,
                        dueSoonOnly,
                        dueWithinDays
                ),
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Follow-ups retrieved successfully", page));
    }

    @GetMapping("/due")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "Doctor due list",
            description = """
                    Open (PENDING/SCHEDULED) follow-ups for the current doctor, including overdue
                    and those due within the next N days (default 14). Optimized for daily worklists.
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<FollowUpResponse>>> dueList(
            @Parameter(description = "Include follow-ups scheduled through today + N days (1–90)")
            @RequestParam(value = "withinDays", required = false) final Integer withinDays,
            @PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Due follow-ups retrieved successfully",
                followUpService.dueList(withinDays, pageable)
        ));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "Get follow-up by id", description = "Direct lookup without consultation path. View is audit-logged.")
    public ResponseEntity<ApiResponse<FollowUpResponse>> getById(
            @PathVariable final UUID id,
            final HttpServletRequest httpRequest
    ) {
        final FollowUpResponse response = followUpService.getByIdGlobal(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Follow-up retrieved successfully", response));
    }
}
