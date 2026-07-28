package com.healthcare.hms.clinical.enums;

/**
 * Clinical role of a diagnosis within a consultation.
 */
public enum DiagnosisType {

    /** Principal reason for the encounter. */
    PRIMARY,

    /** Additional active problems addressed during the visit. */
    SECONDARY,

    /** Working diagnosis pending confirmation. */
    DIFFERENTIAL
}
