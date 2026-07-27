package com.healthcare.hms.appointments.availability.repository;

import com.healthcare.hms.appointments.availability.entity.DoctorSchedule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    Optional<DoctorSchedule> findByIdAndTenantIdAndDoctorId(UUID id, UUID tenantId, UUID doctorId);

    List<DoctorSchedule> findByTenantIdAndDoctorIdOrderByEffectiveFromDesc(UUID tenantId, UUID doctorId);

    @Query("""
            SELECT s FROM DoctorSchedule s
            WHERE s.tenantId = :tenantId
              AND s.doctorId = :doctorId
              AND s.status = com.healthcare.hms.appointments.availability.enums.ScheduleStatus.ACTIVE
              AND s.effectiveFrom <= :date
              AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)
            """)
    List<DoctorSchedule> findActiveCoveringDate(
            @Param("tenantId") UUID tenantId,
            @Param("doctorId") UUID doctorId,
            @Param("date") LocalDate date
    );

    /**
     * Active schedules whose effective range overlaps {@code [from, to]} (null {@code to} = open-ended).
     */
    @Query("""
            SELECT s FROM DoctorSchedule s
            WHERE s.tenantId = :tenantId
              AND s.doctorId = :doctorId
              AND s.status = com.healthcare.hms.appointments.availability.enums.ScheduleStatus.ACTIVE
              AND (:to IS NULL OR s.effectiveFrom <= :to)
              AND (s.effectiveTo IS NULL OR s.effectiveTo >= :from)
              AND (:excludeId IS NULL OR s.id <> :excludeId)
            """)
    List<DoctorSchedule> findOverlappingActiveSchedules(
            @Param("tenantId") UUID tenantId,
            @Param("doctorId") UUID doctorId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("excludeId") UUID excludeId
    );
}
