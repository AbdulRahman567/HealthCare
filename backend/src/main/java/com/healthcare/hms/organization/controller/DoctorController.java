package com.healthcare.hms.organization.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.organization.dto.request.CreateDoctorRequest;
import com.healthcare.hms.organization.dto.request.UpdateDoctorRequest;
import com.healthcare.hms.organization.dto.response.DoctorResponse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.service.DoctorService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

@RestController
@RequestMapping("/api/v1/doctors")
@Tag(name = "Doctors", description = "Doctor employment profile administration")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class DoctorController {

    private final DoctorService service;

    public DoctorController(final DoctorService service) {
        this.service = service;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.DOCTOR_CREATE)
    @Operation(summary = "Create doctor")
    public ResponseEntity<ApiResponse<DoctorResponse>> create(
            @Valid @RequestBody final CreateDoctorRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DoctorResponse response = service.create(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor created successfully", response));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.DOCTOR_READ)
    @Operation(summary = "Get doctor by id")
    public ResponseEntity<ApiResponse<DoctorResponse>> getById(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Doctor retrieved successfully", service.getById(id)));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.DOCTOR_READ)
    @Operation(summary = "Search doctors")
    public ResponseEntity<ApiResponse<PageResponse<DoctorResponse>>> search(
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "employmentStatus", required = false) final EmploymentStatus employmentStatus,
            @RequestParam(value = "departmentId", required = false) final UUID departmentId,
            @PageableDefault(size = 20, sort = "employeeCode", direction = Sort.Direction.ASC) final Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Doctors retrieved successfully",
                service.search(q, employmentStatus, departmentId, pageable)
        ));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.DOCTOR_UPDATE)
    @Operation(summary = "Update doctor")
    public ResponseEntity<ApiResponse<DoctorResponse>> update(
            @PathVariable("id") final UUID id,
            @Valid @RequestBody final UpdateDoctorRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DoctorResponse response = service.update(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionConstants.DOCTOR_DELETE)
    @Operation(summary = "Soft-delete doctor")
    public ResponseEntity<Void> delete(@PathVariable("id") final UUID id, final HttpServletRequest httpRequest) {
        service.delete(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
