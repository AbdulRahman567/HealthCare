package com.healthcare.hms.organization.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.organization.dto.request.CreatePharmacistRequest;
import com.healthcare.hms.organization.dto.request.UpdatePharmacistRequest;
import com.healthcare.hms.organization.dto.response.PharmacistResponse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface PharmacistService {
    PharmacistResponse create(CreatePharmacistRequest request, String ipAddress, String userAgent);
    PharmacistResponse getById(UUID pharmacistId);
    PageResponse<PharmacistResponse> search(String search, EmploymentStatus employmentStatus, UUID departmentId, Pageable pageable);
    PharmacistResponse update(UUID pharmacistId, UpdatePharmacistRequest request, String ipAddress, String userAgent);
    void delete(UUID pharmacistId, String ipAddress, String userAgent);
}
