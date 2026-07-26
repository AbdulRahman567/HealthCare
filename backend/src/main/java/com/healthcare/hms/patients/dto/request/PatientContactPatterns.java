package com.healthcare.hms.patients.dto.request;

/**
 * Shared contact validation patterns for patient registration DTOs.
 */
public final class PatientContactPatterns {

    /**
     * Optional phone: empty, or international / local digits with common separators.
     * Leading {@code +} allowed; 7–29 significant characters after first digit.
     */
    public static final String PHONE_OPTIONAL = "^$|^\\+?[0-9][0-9\\s().-]{6,28}$";

    /**
     * MRN: starts with alphanumeric; may contain underscore and hyphen.
     */
    public static final String MRN = "^[A-Za-z0-9][A-Za-z0-9_-]*$";

    private PatientContactPatterns() {
    }
}
