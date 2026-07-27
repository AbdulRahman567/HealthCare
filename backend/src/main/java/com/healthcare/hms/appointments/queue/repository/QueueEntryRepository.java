package com.healthcare.hms.appointments.queue.repository;

import com.healthcare.hms.appointments.queue.entity.QueueEntry;
import com.healthcare.hms.appointments.queue.enums.QueueEntryStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueEntryRepository extends JpaRepository<QueueEntry, UUID> {

    Optional<QueueEntry> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<QueueEntry> findByTenantIdAndAppointmentId(UUID tenantId, UUID appointmentId);

    boolean existsByTenantIdAndAppointmentId(UUID tenantId, UUID appointmentId);

    List<QueueEntry> findByTenantIdAndQueueIdOrderByQueueNumberAsc(UUID tenantId, UUID queueId);

    List<QueueEntry> findByTenantIdAndQueueIdAndStatusInOrderByQueueNumberAsc(
            UUID tenantId, UUID queueId, Collection<QueueEntryStatus> statuses);

    long countByTenantIdAndQueueIdAndStatus(UUID tenantId, UUID queueId, QueueEntryStatus status);

    boolean existsByTenantIdAndQueueIdAndStatus(UUID tenantId, UUID queueId, QueueEntryStatus status);
}
