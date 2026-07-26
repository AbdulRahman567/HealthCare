package com.healthcare.hms.patients.allergy.enums;

/**
 * Lifecycle of an allergy assertion on the chart.
 *
 * <pre>
 * ACTIVE            — currently clinically relevant
 * INACTIVE          — historically noted; not currently avoided
 * ENTERED_IN_ERROR  — mistaken entry (soft-kept for audit)
 * </pre>
 */
public enum AllergyStatus {

    ACTIVE,
    INACTIVE,
    ENTERED_IN_ERROR
}
