package com.healthcare.hms.organization.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.organization.dto.request.CreateDoctorRequest;
import com.healthcare.hms.organization.dto.request.UpdateDoctorRequest;
import com.healthcare.hms.organization.dto.response.DoctorResponse;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface DoctorService {

    DoctorResponse create(CreateDoctorRequest request, String ipAddress, String userAgent);

    DoctorResponse getById(UUID doctorId);

    PageResponse<DoctorResponse> search(
            String search,
            EmploymentStatus employmentStatus,
            UUID departmentId,
            Pageable pageable
    );

    DoctorResponse update(UUID doctorId, UpdateDoctorRequest request, String ipAddress, String userAgent);

    void delete(UUID doctorId, String ipAddress, String userAgent);
}
