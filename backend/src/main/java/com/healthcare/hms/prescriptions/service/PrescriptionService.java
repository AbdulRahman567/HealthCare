package com.healthcare.hms.prescriptions.service;

import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.prescriptions.dto.request.CancelPrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.request.CreatePrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.request.PrescriptionSearchCriteria;
import com.healthcare.hms.prescriptions.dto.request.UpdatePrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Digital prescription lifecycle (Phase 7.5).
 */
public interface PrescriptionService {

    PrescriptionResponse create(CreatePrescriptionRequest request, String ipAddress, String userAgent);

    PrescriptionResponse getById(UUID prescriptionId, String ipAddress, String userAgent);

    List<PrescriptionResponse> listByConsultation(UUID consultationId);

    PageResponse<PrescriptionResponse> search(PrescriptionSearchCriteria criteria, Pageable pageable);

    PageResponse<PrescriptionResponse> patientHistory(UUID patientId, Pageable pageable);

    PrescriptionResponse update(
            UUID prescriptionId,
            UpdatePrescriptionRequest request,
            String ipAddress,
            String userAgent
    );

    PrescriptionResponse issue(UUID prescriptionId, String ipAddress, String userAgent);

    PrescriptionResponse cancel(
            UUID prescriptionId,
            CancelPrescriptionRequest request,
            String ipAddress,
            String userAgent
    );

    void delete(UUID prescriptionId, String ipAddress, String userAgent);
}
