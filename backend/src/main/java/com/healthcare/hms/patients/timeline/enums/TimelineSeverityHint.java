package com.healthcare.hms.patients.timeline.enums;

/**
 * Relative safety emphasis for timeline cards (allergies / critical clinical facts).
 *
 * <p>Does not replace allergy banner / critical APIs.
 */
public enum TimelineSeverityHint {

    NONE,
    STANDARD,
    HIGH,
    CRITICAL
}
