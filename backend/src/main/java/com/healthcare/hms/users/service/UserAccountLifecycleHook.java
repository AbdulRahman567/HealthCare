package com.healthcare.hms.users.service;

import java.util.UUID;

/**
 * Cross-module hook when a user account loses authentication eligibility
 * (deactivate / suspend). Implemented by organization to clear department heads
 * and suspend linked employment without introducing a users→organization repository cycle.
 */
@FunctionalInterface
public interface UserAccountLifecycleHook {

    void onUserAuthenticationDisabled(UUID tenantId, UUID userId, UUID actorId);
}
