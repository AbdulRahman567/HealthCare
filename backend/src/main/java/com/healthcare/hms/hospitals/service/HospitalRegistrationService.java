package com.healthcare.hms.hospitals.service;

import com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;

/**
 * Atomic hospital onboarding (Phase 2.5).
 */
public interface HospitalRegistrationService {

    /**
     * Creates tenant, default hospital, default roles/permissions, and initial administrator
     * in a single transaction. Any failure rolls back the entire registration.
     */
    HospitalRegistrationResponse register(
            HospitalRegistrationRequest request,
            String ipAddress,
            String userAgent
    );
}
