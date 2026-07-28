package com.healthcare.hms.clinical.service;

import com.healthcare.hms.clinical.dto.request.CompleteConsultationRequest;
import com.healthcare.hms.clinical.dto.request.ConsultationSearchCriteria;
import com.healthcare.hms.clinical.dto.request.CreateConsultationRequest;
import com.healthcare.hms.clinical.dto.request.UpdateConsultationDocumentationRequest;
import com.healthcare.hms.clinical.dto.response.ClinicalSummaryResponse;
import com.healthcare.hms.clinical.dto.response.ConsultationResponse;
import com.healthcare.hms.common.api.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Consultation encounter lifecycle and clinical documentation (Phase 7.2).
 */
public interface ConsultationService {

    ConsultationResponse create(
            CreateConsultationRequest request,
            String ipAddress,
            String userAgent
    );

    ConsultationResponse getById(UUID consultationId, String ipAddress, String userAgent);

    ClinicalSummaryResponse getClinicalSummary(UUID consultationId, String ipAddress, String userAgent);

    PageResponse<ConsultationResponse> search(ConsultationSearchCriteria criteria, Pageable pageable);

    ConsultationResponse updateDocumentation(
            UUID consultationId,
            UpdateConsultationDocumentationRequest request,
            String ipAddress,
            String userAgent
    );

    ConsultationResponse start(UUID consultationId, String ipAddress, String userAgent);

    ConsultationResponse pause(UUID consultationId, String ipAddress, String userAgent);

    ConsultationResponse resume(UUID consultationId, String ipAddress, String userAgent);

    ConsultationResponse complete(
            UUID consultationId,
            CompleteConsultationRequest request,
            String ipAddress,
            String userAgent
    );

    ConsultationResponse cancel(UUID consultationId, String ipAddress, String userAgent);
}
