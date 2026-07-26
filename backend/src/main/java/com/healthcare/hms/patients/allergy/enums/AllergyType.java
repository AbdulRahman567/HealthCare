package com.healthcare.hms.patients.allergy.enums;

/**
 * High-level allergen class for clinical filtering and prescribing checks.
 *
 * <p>Drug allergies must surface before medication orders (Phase 9+).
 */
public enum AllergyType {

    /** Medication / pharmaceutical allergen (e.g. penicillin). */
    DRUG,

    /** Food allergen (e.g. seafood, peanut). */
    FOOD,

    /** Environmental allergen (e.g. dust, pollen, latex). */
    ENVIRONMENTAL,

    /** Other / not yet classified. */
    OTHER
}
