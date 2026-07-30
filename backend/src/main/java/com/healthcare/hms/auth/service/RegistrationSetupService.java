package com.healthcare.hms.auth.service;

/**
 * Service that holds admin registration data between the two steps of hospital onboarding.
 * Step 1 validates and stores admin data; step 2 retrieves and consumes it atomically.
 */
public interface RegistrationSetupService {

    /**
     * Validates admin data (email uniqueness, password strength) and stores it
     * under a generated token. No DB records are created.
     *
     * @return the opaque registration token
     * @throws com.healthcare.hms.common.exception.ConflictException if email already taken
     */
    String createSetup(SetupData data);

    /**
     * Retrieves and atomically consumes (marks used) a registration setup.
     *
     * @return the setup data, or {@code null} if the token is unknown, expired, or already consumed
     */
    SetupData consumeSetup(String token);

    /** Admin data captured during step 1. */
    record SetupData(
            String firstName,
            String lastName,
            String email,
            String password,
            String phone
    ) {}
}
