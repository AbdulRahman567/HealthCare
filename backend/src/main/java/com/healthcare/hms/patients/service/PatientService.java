package com.healthcare.hms.patients.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.patients.dto.request.PatientSearchCriteria;
import com.healthcare.hms.patients.dto.request.RegisterPatientRequest;
import com.healthcare.hms.patients.dto.request.UpdatePatientRequest;
import com.healthcare.hms.patients.dto.response.PatientResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Patient registration and lifecycle within the current tenant.
 *
 * <p>No physical deletion — use {@link #deactivate} / {@link #reactivate}.
 */
public interface PatientService {

    PatientResponse register(RegisterPatientRequest request, String ipAddress, String userAgent);

    PatientResponse getById(UUID patientId, String ipAddress, String userAgent);

    /**
     * Tenant-scoped directory search with DB Specifications, pagination, and sorting.
     */
    PageResponse<PatientResponse> search(PatientSearchCriteria criteria, Pageable pageable);

    PatientResponse update(
            UUID patientId,
            UpdatePatientRequest request,
            String ipAddress,
            String userAgent
    );

    PatientResponse deactivate(UUID patientId, String ipAddress, String userAgent);

    PatientResponse reactivate(UUID patientId, String ipAddress, String userAgent);
}
