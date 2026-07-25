package com.healthcare.hms.organization.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.organization.dto.request.CreateReceptionistRequest;
import com.healthcare.hms.organization.dto.request.UpdateReceptionistRequest;
import com.healthcare.hms.organization.dto.response.ReceptionistResponse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.service.ReceptionistService;
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
@RequestMapping("/api/v1/receptionists")
@Tag(name = "Receptionists", description = "Receptionist employment profile administration")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class ReceptionistController {

    private final ReceptionistService service;

    public ReceptionistController(final ReceptionistService service) {
        this.service = service;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.STAFF_CREATE)
    @Operation(summary = "Create receptionist")
    public ResponseEntity<ApiResponse<ReceptionistResponse>> create(
            @Valid @RequestBody final CreateReceptionistRequest request,
            final HttpServletRequest httpRequest
    ) {
        final ReceptionistResponse response = service.create(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Receptionist created successfully", response));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.STAFF_READ)
    @Operation(summary = "Get receptionist by id")
    public ResponseEntity<ApiResponse<ReceptionistResponse>> getById(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Receptionist retrieved successfully", service.getById(id)));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.STAFF_READ)
    @Operation(summary = "Search receptionists")
    public ResponseEntity<ApiResponse<PageResponse<ReceptionistResponse>>> search(
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "employmentStatus", required = false) final EmploymentStatus employmentStatus,
            @RequestParam(value = "departmentId", required = false) final UUID departmentId,
            @PageableDefault(size = 20, sort = "employeeCode", direction = Sort.Direction.ASC) final Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Receptionists retrieved successfully",
                service.search(q, employmentStatus, departmentId, pageable)
        ));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    @Operation(summary = "Update receptionist")
    public ResponseEntity<ApiResponse<ReceptionistResponse>> update(
            @PathVariable("id") final UUID id,
            @Valid @RequestBody final UpdateReceptionistRequest request,
            final HttpServletRequest httpRequest
    ) {
        final ReceptionistResponse response = service.update(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Receptionist updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionConstants.STAFF_DELETE)
    @Operation(summary = "Soft-delete receptionist")
    public ResponseEntity<Void> delete(@PathVariable("id") final UUID id, final HttpServletRequest httpRequest) {
        service.delete(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
