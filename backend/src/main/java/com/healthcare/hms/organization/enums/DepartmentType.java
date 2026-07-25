package com.healthcare.hms.organization.enums;

/**
 * Classification of a hospital organizational unit (department).
 *
 * <p>Used by the future {@code Department} entity (not created in Phase 4.1).
 * Values are stable vocabulary for clinical vs operational units.
 */
public enum DepartmentType {

    /** Patient-facing clinical specialty (e.g. Cardiology, Orthopedics). */
    CLINICAL,

    /** Diagnostic / ancillary services (e.g. Laboratory, Radiology). */
    DIAGNOSTIC,

    /** Emergency and acute intake units. */
    EMERGENCY,

    /** Hospital administration, HR, finance, operations. */
    ADMINISTRATIVE,

    /** Facilities, housekeeping, logistics, and other support services. */
    SUPPORT,

    /** Research, education, or teaching programs. */
    RESEARCH,

    /** Catch-all for tenant-defined units that do not fit above. */
    OTHER
}
