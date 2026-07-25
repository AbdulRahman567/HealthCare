package com.healthcare.hms.organization.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.organization.dto.request.CreatePharmacistRequest;
import com.healthcare.hms.organization.dto.request.UpdatePharmacistRequest;
import com.healthcare.hms.organization.dto.response.PharmacistResponse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.service.PharmacistService;
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
@RequestMapping("/api/v1/pharmacists")
@Tag(name = "Pharmacists", description = "Pharmacist employment profile administration")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class PharmacistController {

    private final PharmacistService service;

    public PharmacistController(final PharmacistService service) {
        this.service = service;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.STAFF_CREATE)
    @Operation(summary = "Create pharmacist")
    public ResponseEntity<ApiResponse<PharmacistResponse>> create(
            @Valid @RequestBody final CreatePharmacistRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PharmacistResponse response = service.create(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pharmacist created successfully", response));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.STAFF_READ)
    @Operation(summary = "Get pharmacist by id")
    public ResponseEntity<ApiResponse<PharmacistResponse>> getById(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Pharmacist retrieved successfully", service.getById(id)));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.STAFF_READ)
    @Operation(summary = "Search pharmacists")
    public ResponseEntity<ApiResponse<PageResponse<PharmacistResponse>>> search(
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "employmentStatus", required = false) final EmploymentStatus employmentStatus,
            @RequestParam(value = "departmentId", required = false) final UUID departmentId,
            @PageableDefault(size = 20, sort = "employeeCode", direction = Sort.Direction.ASC) final Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pharmacists retrieved successfully",
                service.search(q, employmentStatus, departmentId, pageable)
        ));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    @Operation(summary = "Update pharmacist")
    public ResponseEntity<ApiResponse<PharmacistResponse>> update(
            @PathVariable("id") final UUID id,
            @Valid @RequestBody final UpdatePharmacistRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PharmacistResponse response = service.update(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Pharmacist updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionConstants.STAFF_DELETE)
    @Operation(summary = "Soft-delete pharmacist")
    public ResponseEntity<Void> delete(@PathVariable("id") final UUID id, final HttpServletRequest httpRequest) {
        service.delete(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
