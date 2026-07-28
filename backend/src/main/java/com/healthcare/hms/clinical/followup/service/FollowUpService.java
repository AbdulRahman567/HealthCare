package com.healthcare.hms.clinical.followup.service;

import com.healthcare.hms.clinical.enums.FollowUpStatus;
import com.healthcare.hms.clinical.followup.dto.request.CreateFollowUpRequest;
import com.healthcare.hms.clinical.followup.dto.request.FollowUpSearchCriteria;
import com.healthcare.hms.clinical.followup.dto.request.UpdateFollowUpRequest;
import com.healthcare.hms.clinical.followup.dto.request.UpdateFollowUpStatusRequest;
import com.healthcare.hms.clinical.followup.dto.response.FollowUpResponse;
import com.healthcare.hms.common.api.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Follow-up planning, due lists, status lifecycle, and patient history (Phase 7.4 / 7.7).
 */
public interface FollowUpService {

    FollowUpResponse create(
            UUID consultationId,
            CreateFollowUpRequest request,
            String ipAddress,
            String userAgent
    );

    FollowUpResponse getById(UUID consultationId, UUID followUpId, String ipAddress, String userAgent);

    FollowUpResponse getByIdGlobal(UUID followUpId, String ipAddress, String userAgent);

    List<FollowUpResponse> listByConsultation(UUID consultationId);

    FollowUpResponse update(
            UUID consultationId,
            UUID followUpId,
            UpdateFollowUpRequest request,
            String ipAddress,
            String userAgent
    );

    FollowUpResponse updateStatus(
            UUID consultationId,
            UUID followUpId,
            UpdateFollowUpStatusRequest request,
            String ipAddress,
            String userAgent
    );

    void delete(UUID consultationId, UUID followUpId, String ipAddress, String userAgent);

    PageResponse<FollowUpResponse> patientHistory(
            UUID patientId,
            FollowUpStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );

    PageResponse<FollowUpResponse> search(FollowUpSearchCriteria criteria, Pageable pageable);

    PageResponse<FollowUpResponse> dueList(Integer withinDays, Pageable pageable);
}
