package com.healthcare.hms.appointments.availability.controller;

import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorUnavailabilityRequest;
import com.healthcare.hms.appointments.availability.dto.response.DoctorUnavailabilityResponse;
import com.healthcare.hms.appointments.availability.enums.UnavailabilityType;
import com.healthcare.hms.appointments.availability.service.DoctorUnavailabilityService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Doctor leave, holiday, and emergency unavailability (Phase 6.2).
 */
@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/unavailabilities")
@Tag(name = "Doctor Unavailability", description = "Leave, holiday, and emergency unavailability blocks")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class DoctorUnavailabilityController {

    private final DoctorUnavailabilityService unavailabilityService;

    public DoctorUnavailabilityController(final DoctorUnavailabilityService unavailabilityService) {
        this.unavailabilityService = unavailabilityService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.APPOINTMENT_CREATE)
    @Operation(summary = "Create doctor unavailability")
    public ResponseEntity<ApiResponse<DoctorUnavailabilityResponse>> create(
            @PathVariable("doctorId") final UUID doctorId,
            @Valid @RequestBody final UpsertDoctorUnavailabilityRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DoctorUnavailabilityResponse response = unavailabilityService.create(
                doctorId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor unavailability created successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "List doctor unavailabilities")
    public ResponseEntity<ApiResponse<List<DoctorUnavailabilityResponse>>> list(
            @PathVariable("doctorId") final UUID doctorId,
            @Parameter(description = "Filter by LEAVE, HOLIDAY, or EMERGENCY")
            @RequestParam(value = "type", required = false) final UnavailabilityType type
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Doctor unavailabilities retrieved successfully",
                unavailabilityService.list(doctorId, type)
        ));
    }

    @GetMapping("/{unavailabilityId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Get doctor unavailability by id")
    public ResponseEntity<ApiResponse<DoctorUnavailabilityResponse>> getById(
            @PathVariable("doctorId") final UUID doctorId,
            @PathVariable("unavailabilityId") final UUID unavailabilityId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Doctor unavailability retrieved successfully",
                unavailabilityService.getById(doctorId, unavailabilityId)
        ));
    }

    @PutMapping("/{unavailabilityId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_UPDATE)
    @Operation(summary = "Update doctor unavailability")
    public ResponseEntity<ApiResponse<DoctorUnavailabilityResponse>> update(
            @PathVariable("doctorId") final UUID doctorId,
            @PathVariable("unavailabilityId") final UUID unavailabilityId,
            @Valid @RequestBody final UpsertDoctorUnavailabilityRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DoctorUnavailabilityResponse response = unavailabilityService.update(
                doctorId,
                unavailabilityId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Doctor unavailability updated successfully", response));
    }

    @DeleteMapping("/{unavailabilityId}")
    @RequirePermission(PermissionConstants.APPOINTMENT_DELETE)
    @Operation(summary = "Soft-delete doctor unavailability")
    public ResponseEntity<Void> delete(
            @PathVariable("doctorId") final UUID doctorId,
            @PathVariable("unavailabilityId") final UUID unavailabilityId,
            final HttpServletRequest httpRequest
    ) {
        unavailabilityService.delete(
                doctorId,
                unavailabilityId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
