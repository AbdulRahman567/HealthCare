package com.healthcare.hms.organization.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.organization.dto.request.CreateLaboratoryStaffRequest;
import com.healthcare.hms.organization.dto.request.UpdateLaboratoryStaffRequest;
import com.healthcare.hms.organization.dto.response.LaboratoryStaffResponse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.service.LaboratoryStaffService;
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
@RequestMapping("/api/v1/laboratory-staff")
@Tag(name = "Laboratory Staff", description = "Laboratory staff employment profile administration")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class LaboratoryStaffController {

    private final LaboratoryStaffService service;

    public LaboratoryStaffController(final LaboratoryStaffService service) {
        this.service = service;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.STAFF_CREATE)
    @Operation(summary = "Create laboratory staff")
    public ResponseEntity<ApiResponse<LaboratoryStaffResponse>> create(
            @Valid @RequestBody final CreateLaboratoryStaffRequest request,
            final HttpServletRequest httpRequest
    ) {
        final LaboratoryStaffResponse response = service.create(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("LaboratoryStaff created successfully", response));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.STAFF_READ)
    @Operation(summary = "Get laboratory staff by id")
    public ResponseEntity<ApiResponse<LaboratoryStaffResponse>> getById(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok(ApiResponse.success("LaboratoryStaff retrieved successfully", service.getById(id)));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.STAFF_READ)
    @Operation(summary = "Search laboratory staff")
    public ResponseEntity<ApiResponse<PageResponse<LaboratoryStaffResponse>>> search(
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "employmentStatus", required = false) final EmploymentStatus employmentStatus,
            @RequestParam(value = "departmentId", required = false) final UUID departmentId,
            @PageableDefault(size = 20, sort = "employeeCode", direction = Sort.Direction.ASC) final Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Laboratory Staff retrieved successfully",
                service.search(q, employmentStatus, departmentId, pageable)
        ));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    @Operation(summary = "Update laboratory staff")
    public ResponseEntity<ApiResponse<LaboratoryStaffResponse>> update(
            @PathVariable("id") final UUID id,
            @Valid @RequestBody final UpdateLaboratoryStaffRequest request,
            final HttpServletRequest httpRequest
    ) {
        final LaboratoryStaffResponse response = service.update(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("LaboratoryStaff updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionConstants.STAFF_DELETE)
    @Operation(summary = "Soft-delete laboratory staff")
    public ResponseEntity<Void> delete(@PathVariable("id") final UUID id, final HttpServletRequest httpRequest) {
        service.delete(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
