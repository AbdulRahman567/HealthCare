package com.healthcare.hms.auth.dto.response;

import com.healthcare.hms.hospitals.enums.SubscriptionPlan;
import com.healthcare.hms.hospitals.enums.TenantStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Registered hospital tenant representation.
 */
public record HospitalRegistrationResponse(
        UUID tenantId,
        String name,
        String slug,
        String email,
        String phone,
        String address,
        SubscriptionPlan subscriptionPlan,
        TenantStatus status,
        Instant createdAt
) {
}
