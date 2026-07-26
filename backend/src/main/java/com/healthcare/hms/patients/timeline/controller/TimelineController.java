package com.healthcare.hms.patients.timeline.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.patients.timeline.dto.response.TimelinePageResponse;
import com.healthcare.hms.patients.timeline.enums.TimelineEventType;
import com.healthcare.hms.patients.timeline.enums.TimelineSortDirection;
import com.healthcare.hms.patients.timeline.service.TimelineService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Patient chronological timeline API (Phase 5.6).
 *
 * <p>Aggregates registration, medical history, allergies, and immunizations.
 * Visits, prescriptions, lab results, and billing plug in via
 * {@code TimelineEventProvider} when those modules ship.
 */
@RestController
@RequestMapping("/api/v1/patients/{patientId}/timeline")
@Tag(name = "Patient Timeline", description = "Chronological patient chart feed (cursor-paged)")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(final TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(
            summary = "Patient timeline",
            description = """
                    Chronological clinical story (newest first by default). Sorted by clinical
                    event date, then recorded-at. Allergy banner/critical APIs remain separate
                    safety surfaces — call those on every chart open in addition to timeline.
                    Future VISIT / PRESCRIPTION / LAB_RESULT / BILLING types are reserved;
                    they return no rows until those modules register providers.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Timeline page",
                    content = @Content(schema = @Schema(implementation = TimelinePageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid cursor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<ApiResponse<TimelinePageResponse>> getTimeline(
            @PathVariable("patientId") final UUID patientId,
            @Parameter(description = "Filter by event types (multi). Empty = all available sources.")
            @RequestParam(value = "types", required = false) final Set<TimelineEventType> types,
            @Parameter(description = "Opaque cursor from previous page nextCursor")
            @RequestParam(value = "cursor", required = false) final String cursor,
            @Parameter(description = "Page size (default 20, max 100)")
            @RequestParam(value = "size", required = false, defaultValue = "20") final int size,
            @Parameter(description = "DESC (default, newest first) or ASC")
            @RequestParam(value = "direction", required = false, defaultValue = "DESC")
            final TimelineSortDirection direction
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Patient timeline retrieved successfully",
                timelineService.getTimeline(patientId, types, cursor, size, direction)
        ));
    }
}
