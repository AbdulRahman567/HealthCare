package com.healthcare.hms.hospitals.service;

import com.healthcare.hms.hospitals.dto.request.UpdateHospitalSettingsRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalSettingsResponse;

/**
 * Tenant-scoped hospital settings (profile, locale, contact, address, working hours).
 */
public interface HospitalSettingsService {

    HospitalSettingsResponse getSettings();

    HospitalSettingsResponse updateSettings(
            UpdateHospitalSettingsRequest request,
            String ipAddress,
            String userAgent
    );
}
