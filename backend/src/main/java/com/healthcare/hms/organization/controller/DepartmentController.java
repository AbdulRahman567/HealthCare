package com.healthcare.hms.organization.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.organization.dto.request.CreateDepartmentRequest;
import com.healthcare.hms.organization.dto.request.UpdateDepartmentRequest;
import com.healthcare.hms.organization.dto.response.DepartmentResponse;
import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.DepartmentType;
import com.healthcare.hms.organization.service.DepartmentService;
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

/**
 * Tenant-scoped department management API (Phase 4.2).
 */
@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Departments", description = "Hospital department management within the current tenant")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(final DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.DEPARTMENT_CREATE)
    @Operation(
            summary = "Create department",
            description = """
                    Requires JWT, tenant context, and DEPARTMENT_CREATE.
                    Creates a department under the tenant's default hospital.
                    Code is unique per tenant (case-insensitive, stored uppercase).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Department created",
                    content = @Content(schema = @Schema(implementation = DepartmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate name or code")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody final CreateDepartmentRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DepartmentResponse response = departmentService.create(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", response));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.DEPARTMENT_READ)
    @Operation(summary = "Get department by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Department retrieved",
                    content = @Content(schema = @Schema(implementation = DepartmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Department retrieved successfully",
                departmentService.getById(id)
        ));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.DEPARTMENT_READ)
    @Operation(
            summary = "Search departments",
            description = """
                    Paginated list with optional search (name, code, description, location),
                    status / type / hospital filters, and whitelisted sorting.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> search(
            @Parameter(description = "Search text matched against name, code, description, location")
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "status", required = false) final DepartmentStatus status,
            @RequestParam(value = "type", required = false) final DepartmentType type,
            @RequestParam(value = "hospitalId", required = false) final UUID hospitalId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) final Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Departments retrieved successfully",
                departmentService.search(q, status, type, hospitalId, pageable)
        ));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.DEPARTMENT_UPDATE)
    @Operation(summary = "Update department")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Department updated",
                    content = @Content(schema = @Schema(implementation = DepartmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate name or code")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable("id") final UUID id,
            @Valid @RequestBody final UpdateDepartmentRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DepartmentResponse response = departmentService.update(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionConstants.DEPARTMENT_DELETE)
    @Operation(summary = "Soft-delete department")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Department deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> delete(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        departmentService.delete(
                id,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
