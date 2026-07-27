package com.healthcare.hms.appointments.availability.controller;

import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorScheduleRequest;
import com.healthcare.hms.appointments.availability.dto.response.DoctorScheduleResponse;
import com.healthcare.hms.appointments.availability.service.DoctorScheduleService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Recurring doctor working schedule — days, hours, breaks, max appointments / day (Phase 6.2).
 */
@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/schedules")
@Tag(name = "Doctor Schedules", description = "Doctor recurring availability (working days, hours, breaks)")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    public DoctorScheduleController(final DoctorScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.APPOINTMENT_CREATE)
    @Operation(summary = "Create doctor schedule")
    public ResponseEntity<ApiResponse<DoctorScheduleResponse>> create(
            @PathVariable("doctorId") final UUID doctorId,
            @Valid @RequestBody final UpsertDoctorScheduleRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DoctorScheduleResponse response = scheduleService.create(
                doctorId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor schedule created successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "List doctor schedules")
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponse>>> list(
            @PathVariable("doctorId") final UUID doctorId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Doctor schedules retrieved successfully",
                scheduleService.list(doctorId)
        ));
    }

    @GetMapping("/{scheduleId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Get doctor schedule by id")
    public ResponseEntity<ApiResponse<DoctorScheduleResponse>> getById(
            @PathVariable("doctorId") final UUID doctorId,
            @PathVariable("scheduleId") final UUID scheduleId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Doctor schedule retrieved successfully",
                scheduleService.getById(doctorId, scheduleId)
        ));
    }

    @PutMapping("/{scheduleId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(summary = "Update doctor schedule")
    public ResponseEntity<ApiResponse<DoctorScheduleResponse>> update(
            @PathVariable("doctorId") final UUID doctorId,
            @PathVariable("scheduleId") final UUID scheduleId,
            @Valid @RequestBody final UpsertDoctorScheduleRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DoctorScheduleResponse response = scheduleService.update(
                doctorId,
                scheduleId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Doctor schedule updated successfully", response));
    }

    @DeleteMapping("/{scheduleId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_DELETE)
    @Operation(summary = "Soft-delete doctor schedule")
    public ResponseEntity<Void> delete(
            @PathVariable("doctorId") final UUID doctorId,
            @PathVariable("scheduleId") final UUID scheduleId,
            final HttpServletRequest httpRequest
    ) {
        scheduleService.delete(
                doctorId,
                scheduleId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
