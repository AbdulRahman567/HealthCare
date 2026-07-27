package com.healthcare.hms.appointments.queue.repository;

import com.healthcare.hms.appointments.queue.entity.DoctorDayQueue;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorDayQueueRepository extends JpaRepository<DoctorDayQueue, UUID> {

    Optional<DoctorDayQueue> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<DoctorDayQueue> findByTenantIdAndDoctorIdAndQueueDate(
            UUID tenantId, UUID doctorId, LocalDate queueDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT q FROM DoctorDayQueue q
            WHERE q.tenantId = :tenantId
              AND q.doctorId = :doctorId
              AND q.queueDate = :queueDate
            """)
    Optional<DoctorDayQueue> findForUpdate(
            @Param("tenantId") UUID tenantId,
            @Param("doctorId") UUID doctorId,
            @Param("queueDate") LocalDate queueDate
    );
}
