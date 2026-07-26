package com.healthcare.hms.patients.allergy.service;

import com.healthcare.hms.patients.allergy.dto.request.UpsertAllergyRequest;
import com.healthcare.hms.patients.allergy.dto.response.AllergyBannerResponse;
import com.healthcare.hms.patients.allergy.dto.response.AllergyCriticalAlertResponse;
import com.healthcare.hms.patients.allergy.dto.response.AllergyResponse;
import com.healthcare.hms.patients.allergy.enums.AllergyType;
import java.util.List;
import java.util.UUID;

/**
 * Safety-critical allergy management (Phase 5.4).
 */
public interface AllergyService {

    List<AllergyResponse> list(UUID patientId, AllergyType allergyType);

    AllergyResponse getById(UUID patientId, UUID allergyId);

    AllergyBannerResponse getBannerAlerts(UUID patientId);

    AllergyCriticalAlertResponse getCriticalAlerts(UUID patientId);

    AllergyResponse create(UUID patientId, UpsertAllergyRequest request, String ipAddress, String userAgent);

    AllergyResponse update(
            UUID patientId,
            UUID allergyId,
            UpsertAllergyRequest request,
            String ipAddress,
            String userAgent
    );

    void softDelete(UUID patientId, UUID allergyId, String ipAddress, String userAgent);
}
