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

    /**
     * Creates the real tenant, default hospital, default roles/permissions, and initial
     * administrator in a single transaction, marking the administrator's email as already
     * verified (Phase 7 — invoked at verification-link time). Does NOT issue a verification
     * email. {@code trialEndsAt} is computed from the moment this runs.
     *
     * <p>{@code preHashedPassword} is the already-BCrypt-hashed admin password captured at
     * signup (the raw password is never stored), so it is applied directly rather than
     * re-encoded.
     */
    HospitalRegistrationResponse registerVerified(
            HospitalRegistrationRequest request,
            String preHashedPassword,
            String ipAddress,
            String userAgent
    );
}
