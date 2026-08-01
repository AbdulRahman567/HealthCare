package com.healthcare.hms.auth.service;

import com.healthcare.hms.auth.dto.request.RegistrationRequest;
import com.healthcare.hms.auth.dto.response.PendingRegistrationResponse;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import java.time.Instant;

/**
 * Phase 7 registration: captures the full signup payload as a transient pending record and
 * creates NO real tenant/hospital/admin account until the emailed verification link is used.
 */
public interface PendingRegistrationService {

    /**
     * Validates the signup, stores a lightweight pending record, and sends the verification
     * email. No real tenant/hospital/admin/user is created.
     */
    PendingRegistrationResponse submit(RegistrationRequest request, String ipAddress, String userAgent);

    /**
     * Issues a fresh verification token for an existing, unverified pending registration
     * (used by "resend verification email").
     */
    PendingRegistrationResponse resendVerification(String email, String ipAddress, String userAgent);

    /**
     * Consumes a pending-registration verification token, creates the real tenant/hospital/
     * admin records (marking the admin verified), and removes the pending record.
     */
    HospitalRegistrationResponse verify(String rawToken, String ipAddress, String userAgent);

    /**
     * Hard-deletes expired, unverified pending registrations. Returns rows removed.
     */
    int deleteExpired(Instant now);
}
