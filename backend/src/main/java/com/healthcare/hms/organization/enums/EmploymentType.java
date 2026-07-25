package com.healthcare.hms.organization.enums;

/**
 * Contractual classification of how a staff member is engaged by the hospital.
 */
public enum EmploymentType {

    /** Standard full-time employee. */
    FULL_TIME,

    /** Part-time employee with reduced contracted hours. */
    PART_TIME,

    /** Fixed-term or project contract (non-permanent). */
    CONTRACT,

    /** Short-term temporary coverage. */
    TEMPORARY,

    /** Trainee / student placement under supervision. */
    INTERN,

    /** External specialist engaged for sessions or referrals. */
    CONSULTANT
}
