package com.healthcare.hms.organization.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.organization.dto.request.CreateDepartmentRequest;
import com.healthcare.hms.organization.dto.request.UpdateDepartmentRequest;
import com.healthcare.hms.organization.dto.response.DepartmentResponse;
import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.DepartmentType;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Department CRUD and search within the current tenant.
 */
public interface DepartmentService {

    DepartmentResponse create(CreateDepartmentRequest request, String ipAddress, String userAgent);

    DepartmentResponse getById(UUID departmentId);

    PageResponse<DepartmentResponse> search(
            String search,
            DepartmentStatus status,
            DepartmentType departmentType,
            UUID hospitalId,
            Pageable pageable
    );

    DepartmentResponse update(
            UUID departmentId,
            UpdateDepartmentRequest request,
            String ipAddress,
            String userAgent
    );

    void delete(UUID departmentId, String ipAddress, String userAgent);
}
