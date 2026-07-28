package com.healthcare.hms.clinical.diagnosis.service;

import com.healthcare.hms.clinical.diagnosis.dto.request.CreateDiagnosisRequest;
import com.healthcare.hms.clinical.diagnosis.dto.request.UpdateDiagnosisRequest;
import com.healthcare.hms.clinical.diagnosis.dto.response.DiagnosisResponse;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import com.healthcare.hms.clinical.enums.DiagnosisType;
import com.healthcare.hms.common.api.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Consultation diagnosis management and patient history (Phase 7.4).
 */
public interface DiagnosisService {

    DiagnosisResponse create(
            UUID consultationId,
            CreateDiagnosisRequest request,
            String ipAddress,
            String userAgent
    );

    DiagnosisResponse getById(UUID consultationId, UUID diagnosisId, String ipAddress, String userAgent);

    List<DiagnosisResponse> listByConsultation(UUID consultationId);

    DiagnosisResponse update(
            UUID consultationId,
            UUID diagnosisId,
            UpdateDiagnosisRequest request,
            String ipAddress,
            String userAgent
    );

    void delete(UUID consultationId, UUID diagnosisId, String ipAddress, String userAgent);

    PageResponse<DiagnosisResponse> patientHistory(
            UUID patientId,
            DiagnosisType diagnosisType,
            DiagnosisStatus diagnosisStatus,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );
}
