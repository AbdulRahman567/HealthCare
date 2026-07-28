package com.healthcare.hms.clinical.enums;

/**
 * Lifecycle status of a patient consultation (clinical encounter).
 *
 * <pre>
 * DRAFT        — created but not yet started (pre-charting)
 * IN_PROGRESS  — doctor is actively documenting the encounter
 * PAUSED       — temporarily suspended (doctor stepped away)
 * COMPLETED    — encounter closed; chart is read-only unless amended
 * CANCELLED    — encounter voided (e.g. patient left before seen)
 * </pre>
 */
public enum ConsultationStatus {

    DRAFT,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    CANCELLED
}
