package com.healthcare.hms.hospitals.dto.response;

import com.healthcare.hms.hospitals.enums.HospitalStatus;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import com.healthcare.hms.tenant.enums.TenantStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Result of atomic hospital registration (tenant, default hospital, admin, roles).
 */
public record HospitalRegistrationResponse(
        UUID tenantId,
        String tenantSlug,
        TenantStatus tenantStatus,
        UUID hospitalId,
        String hospitalName,
        String hospitalCode,
        HospitalStatus hospitalStatus,
        boolean defaultHospital,
        String hospitalEmail,
        String hospitalPhone,
        String hospitalAddress,
        SubscriptionPlan subscriptionPlan,
        UUID adminUserId,
        String adminEmail,
        boolean adminEmailVerified,
        List<String> provisionedRoles,
        Instant createdAt
) {
}
