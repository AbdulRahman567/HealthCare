package com.healthcare.hms.clinical.vitals.service;

import com.healthcare.hms.clinical.vitals.dto.request.RecordVitalSignsRequest;
import com.healthcare.hms.clinical.vitals.dto.request.UpdateVitalSignsRequest;
import com.healthcare.hms.clinical.vitals.dto.response.VitalSignsResponse;
import com.healthcare.hms.common.api.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Vital-signs measurement and patient history (Phase 7.3).
 */
public interface VitalSignsService {

    VitalSignsResponse record(
            UUID consultationId,
            RecordVitalSignsRequest request,
            String ipAddress,
            String userAgent
    );

    VitalSignsResponse getById(UUID consultationId, UUID vitalSignsId, String ipAddress, String userAgent);

    List<VitalSignsResponse> listByConsultation(UUID consultationId);

    VitalSignsResponse update(
            UUID consultationId,
            UUID vitalSignsId,
            UpdateVitalSignsRequest request,
            String ipAddress,
            String userAgent
    );

    void delete(UUID consultationId, UUID vitalSignsId, String ipAddress, String userAgent);

    PageResponse<VitalSignsResponse> patientHistory(
            UUID patientId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );
}
