package com.healthcare.hms.organization.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.organization.dto.request.AssignDepartmentHeadRequest;
import com.healthcare.hms.organization.dto.request.AssignStaffRequest;
import com.healthcare.hms.organization.dto.request.TransferStaffRequest;
import com.healthcare.hms.organization.dto.response.DepartmentResponse;
import com.healthcare.hms.organization.dto.response.StaffAssignmentResponse;
import com.healthcare.hms.organization.enums.StaffType;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Staff ↔ department assignment operations (Phase 4.4).
 */
public interface StaffAssignmentService {

    StaffAssignmentResponse assign(AssignStaffRequest request, String ipAddress, String userAgent);

    StaffAssignmentResponse transfer(TransferStaffRequest request, String ipAddress, String userAgent);

    DepartmentResponse assignDepartmentHead(
            UUID departmentId,
            AssignDepartmentHeadRequest request,
            String ipAddress,
            String userAgent
    );

    DepartmentResponse clearDepartmentHead(UUID departmentId, String ipAddress, String userAgent);

    StaffAssignmentResponse getCurrent(StaffType staffType, UUID staffId);

    PageResponse<StaffAssignmentResponse> history(
            StaffType staffType,
            UUID staffId,
            UUID departmentId,
            Pageable pageable
    );
}
