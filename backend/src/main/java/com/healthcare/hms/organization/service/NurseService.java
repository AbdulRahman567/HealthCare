package com.healthcare.hms.organization.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.organization.dto.request.CreateNurseRequest;
import com.healthcare.hms.organization.dto.request.UpdateNurseRequest;
import com.healthcare.hms.organization.dto.response.NurseResponse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface NurseService {
    NurseResponse create(CreateNurseRequest request, String ipAddress, String userAgent);
    NurseResponse getById(UUID nurseId);
    PageResponse<NurseResponse> search(String search, EmploymentStatus employmentStatus, UUID departmentId, Pageable pageable);
    NurseResponse update(UUID nurseId, UpdateNurseRequest request, String ipAddress, String userAgent);
    void delete(UUID nurseId, String ipAddress, String userAgent);
}
