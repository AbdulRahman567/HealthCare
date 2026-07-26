package com.healthcare.hms.patients.immunization.service;

import com.healthcare.hms.patients.immunization.dto.request.UpsertImmunizationRequest;
import com.healthcare.hms.patients.immunization.dto.response.ImmunizationDueResponse;
import com.healthcare.hms.patients.immunization.dto.response.ImmunizationResponse;
import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import java.util.List;
import java.util.UUID;

/**
 * Patient immunization management (Phase 5.5).
 */
public interface ImmunizationService {

    List<ImmunizationResponse> list(UUID patientId, ImmunizationStatus status);

    ImmunizationResponse getById(UUID patientId, UUID immunizationId);

    ImmunizationDueResponse getDue(UUID patientId);

    ImmunizationResponse create(UUID patientId, UpsertImmunizationRequest request, String ipAddress, String userAgent);

    ImmunizationResponse update(
            UUID patientId,
            UUID immunizationId,
            UpsertImmunizationRequest request,
            String ipAddress,
            String userAgent
    );

    void softDelete(UUID patientId, UUID immunizationId, String ipAddress, String userAgent);
}
