package com.healthcare.hms.appointments.reminder.repository;

import com.healthcare.hms.appointments.reminder.entity.AppointmentReminder;
import com.healthcare.hms.appointments.reminder.enums.ReminderChannel;
import com.healthcare.hms.appointments.reminder.enums.ReminderStatus;
import com.healthcare.hms.appointments.reminder.enums.ReminderType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentReminderRepository extends JpaRepository<AppointmentReminder, UUID> {

    Optional<AppointmentReminder> findByIdAndTenantId(UUID id, UUID tenantId);

    List<AppointmentReminder> findByTenantIdAndAppointmentIdOrderByScheduledAtAsc(
            UUID tenantId, UUID appointmentId);

    List<AppointmentReminder> findByTenantIdAndAppointmentIdAndStatusIn(
            UUID tenantId, UUID appointmentId, Collection<ReminderStatus> statuses);

    boolean existsByTenantIdAndAppointmentIdAndChannelAndReminderTypeAndLeadOffsetMinutesAndStatus(
            UUID tenantId,
            UUID appointmentId,
            ReminderChannel channel,
            ReminderType reminderType,
            Integer leadOffsetMinutes,
            ReminderStatus status
    );

    /**
     * Due PENDING reminders for background dispatch (ordered oldest first).
     */
    @Query("""
            SELECT r FROM AppointmentReminder r
            WHERE r.status = :status
              AND r.scheduledAt <= :now
            ORDER BY r.scheduledAt ASC
            """)
    List<AppointmentReminder> findDueForDispatch(
            @Param("status") ReminderStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );
}
