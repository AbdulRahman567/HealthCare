package com.healthcare.hms.organization.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.organization.dto.request.CreateLaboratoryStaffRequest;
import com.healthcare.hms.organization.dto.request.UpdateLaboratoryStaffRequest;
import com.healthcare.hms.organization.dto.response.LaboratoryStaffResponse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface LaboratoryStaffService {
    LaboratoryStaffResponse create(CreateLaboratoryStaffRequest request, String ipAddress, String userAgent);
    LaboratoryStaffResponse getById(UUID staffId);
    PageResponse<LaboratoryStaffResponse> search(String search, EmploymentStatus employmentStatus, UUID departmentId, Pageable pageable);
    LaboratoryStaffResponse update(UUID staffId, UpdateLaboratoryStaffRequest request, String ipAddress, String userAgent);
    void delete(UUID staffId, String ipAddress, String userAgent);
}
