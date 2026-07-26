/**
 * Patient chronological timeline (Phase 5.6).
 *
 * <p>Read-model aggregator over registration, medical history, allergies, and
 * immunizations. Future modules (visits, prescriptions, lab, billing) plug in via
 * {@link com.healthcare.hms.patients.timeline.spi.TimelineEventProvider} — no
 * materialised timeline table and no placeholder events.
 *
 * <p>Safety-critical allergy banner/critical APIs remain separate; the timeline is
 * a longitudinal story view, not a substitute for must-not-miss surfaces.
 */
package com.healthcare.hms.patients.timeline;
