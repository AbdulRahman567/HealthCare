package com.healthcare.hms.appointments.controller;

import com.healthcare.hms.appointments.dto.request.AppointmentSearchCriteria;
import com.healthcare.hms.appointments.dto.request.CancelAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.CreateAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.RescheduleAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.UpdateAppointmentRequest;
import com.healthcare.hms.appointments.dto.response.AppointmentResponse;
import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.enums.VisitType;
import com.healthcare.hms.appointments.queue.enums.QueueEntryStatus;
import com.healthcare.hms.appointments.service.AppointmentService;
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
 * Appointment booking and search API (Phases 6.3 / 6.6).
 *
 * <p>Lifecycle: create → confirm (optional) → cancel / reschedule / (later complete).
 * Enforces no double-booking, no past slots, doctor availability, and patient conflicts.
 * Directory search uses JPA Specifications with database-only filtering.
 */
@RestController
@RequestMapping("/api/v1/appointments")
@Validated
@Tag(name = "Appointments", description = "Patient appointment booking, search, reschedule, cancel, and confirm")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(final AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.APPOINTMENT_CREATE)
    @Operation(
            summary = "Book appointment",
            description = """
                    Creates a SCHEDULED appointment for an ACTIVE patient with an ACTIVE doctor.
                    Validates: not in the past (hospital timezone), doctor working hours / breaks /
                    leave-holiday-emergency, daily capacity, and no doctor or patient slot overlaps.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Appointment booked",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient/doctor/department not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Double booking or capacity conflict")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(
            @Valid @RequestBody final CreateAppointmentRequest request,
            final HttpServletRequest httpRequest
    ) {
        final AppointmentResponse response = appointmentService.create(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment booked successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(
            summary = "Search appointments",
            description = """
                    Paginated, sorted directory search using JPA Specifications (database filters only).
                    Supports appointment number (prefix), patient id/name, doctor id/name, department
                    id/name, status, visit type, inclusive date range, and queue status.
                    Cross-module name and queue filters use EXISTS subqueries — no in-memory filtering.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filters or sort"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> search(
            @Parameter(description = "Appointment number prefix (case-insensitive)")
            @RequestParam(value = "appointmentNumber", required = false)
            @Size(max = 100) final String appointmentNumber,
            @RequestParam(value = "patientId", required = false) final UUID patientId,
            @Parameter(description = "Patient first, last, or full name (contains)")
            @RequestParam(value = "patientName", required = false)
            @Size(max = 100) final String patientName,
            @RequestParam(value = "doctorId", required = false) final UUID doctorId,
            @Parameter(description = "Doctor name or employee code (contains / prefix)")
            @RequestParam(value = "doctorName", required = false)
            @Size(max = 100) final String doctorName,
            @RequestParam(value = "departmentId", required = false) final UUID departmentId,
            @Parameter(description = "Department name (contains) or code (prefix)")
            @RequestParam(value = "departmentName", required = false)
            @Size(max = 100) final String departmentName,
            @RequestParam(value = "status", required = false) final AppointmentStatus status,
            @RequestParam(value = "visitType", required = false) final VisitType visitType,
            @Parameter(description = "Inclusive start date")
            @RequestParam(value = "fromDate", required = false) final LocalDate fromDate,
            @Parameter(description = "Inclusive end date")
            @RequestParam(value = "toDate", required = false) final LocalDate toDate,
            @Parameter(description = "Latest queue entry status for the appointment")
            @RequestParam(value = "queueStatus", required = false) final QueueEntryStatus queueStatus,
            @PageableDefault(size = 20, sort = "appointmentDate", direction = Sort.Direction.DESC)
            final Pageable pageable
    ) {
        final AppointmentSearchCriteria criteria = new AppointmentSearchCriteria(
                appointmentNumber,
                patientId,
                patientName,
                doctorId,
                doctorName,
                departmentId,
                departmentName,
                status,
                visitType,
                fromDate,
                toDate,
                queueStatus
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Appointments retrieved successfully",
                appointmentService.search(criteria, pageable)
        ));
    }

    @GetMapping("/{appointmentId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Get appointment by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointment found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> getById(
            @PathVariable("appointmentId") final UUID appointmentId,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Appointment retrieved successfully",
                appointmentService.getById(
                        appointmentId,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @PutMapping("/{appointmentId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(
            summary = "Update appointment",
            description = "Updates slot and classification for a SCHEDULED or CONFIRMED appointment. Same conflict rules as booking."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> update(
            @PathVariable("appointmentId") final UUID appointmentId,
            @Valid @RequestBody final UpdateAppointmentRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Appointment updated successfully",
                appointmentService.update(
                        appointmentId,
                        request,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @PatchMapping("/{appointmentId}/reschedule")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(
            summary = "Reschedule appointment",
            description = """
                    Moves the appointment to a new date/time (and optional doctor/department).
                    Clears confirmation so the new slot must be re-confirmed.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rescheduled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> reschedule(
            @PathVariable("appointmentId") final UUID appointmentId,
            @Valid @RequestBody final RescheduleAppointmentRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Appointment rescheduled successfully",
                appointmentService.reschedule(
                        appointmentId,
                        request,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @PatchMapping("/{appointmentId}/cancel")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(summary = "Cancel appointment", description = "Cancels a SCHEDULED or CONFIRMED appointment.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cancelled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid transition")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(
            @PathVariable("appointmentId") final UUID appointmentId,
            @Valid @RequestBody(required = false) final CancelAppointmentRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Appointment cancelled successfully",
                appointmentService.cancel(
                        appointmentId,
                        request == null ? new CancelAppointmentRequest(null) : request,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @PatchMapping("/{appointmentId}/confirm")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(
            summary = "Confirm appointment",
            description = "Transitions SCHEDULED → CONFIRMED. Idempotent if already confirmed."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Confirmed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid transition")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirm(
            @PathVariable("appointmentId") final UUID appointmentId,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Appointment confirmed successfully",
                appointmentService.confirm(
                        appointmentId,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }
}
