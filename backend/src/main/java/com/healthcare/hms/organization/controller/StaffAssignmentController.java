package com.healthcare.hms.organization.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.organization.dto.request.AssignDepartmentHeadRequest;
import com.healthcare.hms.organization.dto.request.AssignStaffRequest;
import com.healthcare.hms.organization.dto.request.TransferStaffRequest;
import com.healthcare.hms.organization.dto.response.DepartmentResponse;
import com.healthcare.hms.organization.dto.response.StaffAssignmentResponse;
import com.healthcare.hms.organization.enums.StaffType;
import com.healthcare.hms.organization.service.StaffAssignmentService;
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
 * Staff department assignment, transfer, history, and department head APIs (Phase 4.4).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Staff Assignments", description = "Assign/transfer staff to departments and manage department heads")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class StaffAssignmentController {

    private final StaffAssignmentService staffAssignmentService;

    public StaffAssignmentController(final StaffAssignmentService staffAssignmentService) {
        this.staffAssignmentService = staffAssignmentService;
    }

    @PostMapping("/staff-assignments")
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    @Operation(
            summary = "Assign staff to a department",
            description = """
                    Requires JWT, tenant context, and STAFF_UPDATE.
                    Staff must not already have a department (use transfer otherwise).
                    Rejects duplicate assignment to the same department.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Assigned",
                    content = @Content(schema = @Schema(implementation = StaffAssignmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Staff or department not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate assignment")
    })
    public ResponseEntity<ApiResponse<StaffAssignmentResponse>> assign(
            @Valid @RequestBody final AssignStaffRequest request,
            final HttpServletRequest httpRequest
    ) {
        final StaffAssignmentResponse response = staffAssignmentService.assign(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff assigned to department successfully", response));
    }

    @PostMapping("/staff-assignments/transfers")
    @RequirePermission(PermissionConstants.STAFF_UPDATE)
    @Operation(
            summary = "Transfer staff between departments",
            description = """
                    Closes the open assignment history row, updates staff.departmentId,
                    and opens a TRANSFER history entry. Clears department-head role if
                    the staff was head of the source department.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transferred",
                    content = @Content(schema = @Schema(implementation = StaffAssignmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already in target department")
    })
    public ResponseEntity<ApiResponse<StaffAssignmentResponse>> transfer(
            @Valid @RequestBody final TransferStaffRequest request,
            final HttpServletRequest httpRequest
    ) {
        final StaffAssignmentResponse response = staffAssignmentService.transfer(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Staff transferred successfully", response));
    }

    @GetMapping("/staff-assignments/current")
    @RequirePermission(PermissionConstants.STAFF_READ)
    @Operation(summary = "Get open department assignment for a staff member")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Current assignment",
                    content = @Content(schema = @Schema(implementation = StaffAssignmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No open assignment")
    })
    public ResponseEntity<ApiResponse<StaffAssignmentResponse>> getCurrent(
            @RequestParam("staffType") final StaffType staffType,
            @RequestParam("staffId") final UUID staffId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Current assignment retrieved successfully",
                staffAssignmentService.getCurrent(staffType, staffId)
        ));
    }

    @GetMapping("/staff-assignments")
    @RequirePermission(PermissionConstants.STAFF_READ)
    @Operation(
            summary = "List assignment history",
            description = "Filter by staffType+staffId or by departmentId (one filter mode required)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History page"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing filter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PageResponse<StaffAssignmentResponse>>> history(
            @Parameter(description = "Staff specialization (required with staffId)")
            @RequestParam(value = "staffType", required = false) final StaffType staffType,
            @RequestParam(value = "staffId", required = false) final UUID staffId,
            @RequestParam(value = "departmentId", required = false) final UUID departmentId,
            @PageableDefault(size = 20, sort = "assignedAt", direction = Sort.Direction.DESC) final Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Assignment history retrieved successfully",
                staffAssignmentService.history(staffType, staffId, departmentId, pageable)
        ));
    }

    @PutMapping("/departments/{departmentId}/head")
    @RequirePermission(PermissionConstants.DEPARTMENT_UPDATE)
    @Operation(
            summary = "Assign department head",
            description = "Staff must be currently assigned to the department. Syncs headStaffId/Type and headUserId."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Head assigned",
                    content = @Content(schema = @Schema(implementation = DepartmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> assignHead(
            @PathVariable("departmentId") final UUID departmentId,
            @Valid @RequestBody final AssignDepartmentHeadRequest request,
            final HttpServletRequest httpRequest
    ) {
        final DepartmentResponse response = staffAssignmentService.assignDepartmentHead(
                departmentId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Department head assigned successfully", response));
    }

    @DeleteMapping("/departments/{departmentId}/head")
    @RequirePermission(PermissionConstants.DEPARTMENT_UPDATE)
    @Operation(summary = "Clear department head")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Head cleared",
                    content = @Content(schema = @Schema(implementation = DepartmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> clearHead(
            @PathVariable("departmentId") final UUID departmentId,
            final HttpServletRequest httpRequest
    ) {
        final DepartmentResponse response = staffAssignmentService.clearDepartmentHead(
                departmentId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Department head cleared successfully", response));
    }
}
