package com.healthcare.hms.appointments.queue.spi;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Clinical encounter bridge used by the OPD queue (Phase 7.10).
 *
 * <p>Implemented by the clinical module. Keeps appointments → clinical dependency
 * to this SPI only (no circular package imports of clinical services).
 */
public interface ConsultationEncounterGateway {

    /**
     * Ensures a live consultation exists for the appointment and is {@code IN_PROGRESS}.
     * Creates + starts when missing; resumes paused / starts draft when present.
     *
     * @return consultation id
     */
    UUID ensureStartedForAppointment(UUID appointmentId, String ipAddress, String userAgent);

    /**
     * @return consultation id when a live (non-deleted) consultation is linked to the appointment
     */
    Optional<UUID> findConsultationIdByAppointment(UUID appointmentId);

    /**
     * Batch lookup of consultation ids keyed by appointment id (missing appointments omitted).
     */
    Map<UUID, UUID> findConsultationIdsByAppointments(Collection<UUID> appointmentIds);

    /**
     * @return true when a linked consultation is still editable (DRAFT / IN_PROGRESS / PAUSED)
     */
    boolean hasOpenConsultation(UUID appointmentId);
}
