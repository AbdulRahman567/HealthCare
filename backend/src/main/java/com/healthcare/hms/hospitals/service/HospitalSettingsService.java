package com.healthcare.hms.hospitals.service;

import com.healthcare.hms.hospitals.dto.request.UpdateHospitalSettingsRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalSettingsResponse;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;

/**
 * Tenant-scoped hospital settings (profile, locale, contact, address, working hours).
 *
 * <p>Service-layer annotations provide defense-in-depth if callers bypass the controller.
 */
public interface HospitalSettingsService {

    @RequirePermission(PermissionConstants.HOSPITAL_READ)
    HospitalSettingsResponse getSettings();

    @RequirePermission(PermissionConstants.HOSPITAL_UPDATE)
    HospitalSettingsResponse updateSettings(
            UpdateHospitalSettingsRequest request,
            String ipAddress,
            String userAgent
    );
}
