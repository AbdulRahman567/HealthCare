package com.healthcare.hms.appointments.queue.controller;

import com.healthcare.hms.appointments.queue.dto.request.CheckInQueueRequest;
import com.healthcare.hms.appointments.queue.dto.request.QueueStatusUpdateRequest;
import com.healthcare.hms.appointments.queue.dto.response.DoctorDayQueueResponse;
import com.healthcare.hms.appointments.queue.dto.response.QueueEntryResponse;
import com.healthcare.hms.appointments.queue.service.QueueService;
import com.healthcare.hms.common.api.ApiResponse;
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
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Doctor daily OPD queue API (Phase 6.4).
 *
 * <p>One queue per doctor per day; check-in assigns an automatic queue number;
 * entries are returned in chronological (queue-number) order.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Queue Management", description = "Daily doctor queues: check-in and status updates")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class QueueController {

    private final QueueService queueService;

    public QueueController(final QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/queues/check-in")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(
            summary = "Check in appointment to daily queue",
            description = """
                    Registers a SCHEDULED/CONFIRMED appointment on the doctor's queue for the
                    appointment date. Creates the daily queue if needed and assigns the next
                    automatic queue number (monotonic per doctor/day).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Checked in",
                    content = @Content(schema = @Schema(implementation = QueueEntryResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already checked in")
    })
    public ResponseEntity<ApiResponse<QueueEntryResponse>> checkIn(
            @Valid @RequestBody final CheckInQueueRequest request,
            final HttpServletRequest httpRequest
    ) {
        final QueueEntryResponse response = queueService.checkIn(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Checked in to queue successfully", response));
    }

    @GetMapping("/queues")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(
            summary = "Get doctor's daily queue",
            description = "Returns the queue with entries ordered by queue number (chronological check-in order)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Queue retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No queue for that day")
    })
    public ResponseEntity<ApiResponse<DoctorDayQueueResponse>> getDailyQueue(
            @RequestParam("doctorId") final UUID doctorId,
            @Parameter(description = "Queue date (defaults to today)")
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Daily queue retrieved successfully",
                queueService.getDailyQueue(doctorId, date)
        ));
    }

    @GetMapping("/queues/{queueId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Get queue by id")
    public ResponseEntity<ApiResponse<DoctorDayQueueResponse>> getQueueById(
            @PathVariable("queueId") final UUID queueId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Queue retrieved successfully",
                queueService.getQueueById(queueId)
        ));
    }

    @GetMapping("/queue-entries/{entryId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Get queue entry by id")
    public ResponseEntity<ApiResponse<QueueEntryResponse>> getEntry(
            @PathVariable("entryId") final UUID entryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Queue entry retrieved successfully",
                queueService.getEntryById(entryId)
        ));
    }

    @PatchMapping("/queue-entries/{entryId}/waiting")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(summary = "Move entry to WAITING", description = "CHECKED_IN → WAITING")
    public ResponseEntity<ApiResponse<QueueEntryResponse>> markWaiting(
            @PathVariable("entryId") final UUID entryId,
            @Valid @RequestBody(required = false) final QueueStatusUpdateRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Queue entry marked WAITING",
                queueService.markWaiting(entryId, request, ip(httpRequest), ua(httpRequest))
        ));
    }

    @PatchMapping("/queue-entries/{entryId}/start-consultation")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(
            summary = "Start consultation",
            description = "WAITING → IN_CONSULTATION. Only one IN_CONSULTATION allowed per doctor/day."
    )
    public ResponseEntity<ApiResponse<QueueEntryResponse>> startConsultation(
            @PathVariable("entryId") final UUID entryId,
            @Valid @RequestBody(required = false) final QueueStatusUpdateRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Consultation started",
                queueService.startConsultation(entryId, request, ip(httpRequest), ua(httpRequest))
        ));
    }

    @PatchMapping("/queue-entries/{entryId}/complete")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(summary = "Complete consultation", description = "IN_CONSULTATION → COMPLETED; syncs appointment to COMPLETED.")
    public ResponseEntity<ApiResponse<QueueEntryResponse>> complete(
            @PathVariable("entryId") final UUID entryId,
            @Valid @RequestBody(required = false) final QueueStatusUpdateRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Consultation completed",
                queueService.complete(entryId, request, ip(httpRequest), ua(httpRequest))
        ));
    }

    @PatchMapping("/queue-entries/{entryId}/missed")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(summary = "Mark missed", description = "CHECKED_IN|WAITING → MISSED; syncs appointment to MISSED.")
    public ResponseEntity<ApiResponse<QueueEntryResponse>> markMissed(
            @PathVariable("entryId") final UUID entryId,
            @Valid @RequestBody(required = false) final QueueStatusUpdateRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Queue entry marked MISSED",
                queueService.markMissed(entryId, request, ip(httpRequest), ua(httpRequest))
        ));
    }

    @PatchMapping("/queue-entries/{entryId}/cancel")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(summary = "Cancel queue entry", description = "Removes patient from active queue (non-terminal → CANCELLED).")
    public ResponseEntity<ApiResponse<QueueEntryResponse>> cancel(
            @PathVariable("entryId") final UUID entryId,
            @Valid @RequestBody(required = false) final QueueStatusUpdateRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Queue entry cancelled",
                queueService.cancel(entryId, request, ip(httpRequest), ua(httpRequest))
        ));
    }

    private static String ip(final HttpServletRequest request) {
        return ClientRequestDetails.resolveClientIp(request);
    }

    private static String ua(final HttpServletRequest request) {
        return ClientRequestDetails.resolveUserAgent(request);
    }
}
