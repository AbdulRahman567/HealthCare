package com.healthcare.hms.organization.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.organization.dto.request.CreateReceptionistRequest;
import com.healthcare.hms.organization.dto.request.UpdateReceptionistRequest;
import com.healthcare.hms.organization.dto.response.ReceptionistResponse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface ReceptionistService {
    ReceptionistResponse create(CreateReceptionistRequest request, String ipAddress, String userAgent);
    ReceptionistResponse getById(UUID receptionistId);
    PageResponse<ReceptionistResponse> search(String search, EmploymentStatus employmentStatus, UUID departmentId, Pageable pageable);
    ReceptionistResponse update(UUID receptionistId, UpdateReceptionistRequest request, String ipAddress, String userAgent);
    void delete(UUID receptionistId, String ipAddress, String userAgent);
}
