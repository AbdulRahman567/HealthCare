package com.healthcare.hms.hospitals.dto.response;

import com.healthcare.hms.hospitals.enums.HospitalStatus;
import com.healthcare.hms.hospitals.model.WorkingHours;
import java.time.Instant;
import java.util.UUID;

/**
 * Tenant-scoped hospital settings view (profile, locale, contact, address, hours).
 */
public record HospitalSettingsResponse(
        UUID hospitalId,
        UUID tenantId,
        String name,
        String code,
        String description,
        String logoUrl,
        String timezone,
        String currency,
        String language,
        String email,
        String phone,
        String secondaryPhone,
        String website,
        String address,
        String city,
        String stateProvince,
        String country,
        String postalCode,
        WorkingHours workingHours,
        boolean defaultHospital,
        HospitalStatus status,
        Instant updatedAt
) {
}
