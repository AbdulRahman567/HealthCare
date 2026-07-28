package com.healthcare.hms.prescriptions.enums;

/**
 * Lifecycle of a digital prescription (pharmacy-ready).
 */
public enum PrescriptionStatus {

    /** Being authored; line items mutable. */
    DRAFT,

    /** Signed / released by the prescribing doctor. */
    ISSUED,

    /** Pharmacy has partially filled the order (future integration). */
    PARTIALLY_DISPENSED,

    /** Fully dispensed by pharmacy (future integration). */
    DISPENSED,

    /** Voided; not dispensable. */
    CANCELLED
}
