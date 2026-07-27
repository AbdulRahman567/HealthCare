package com.healthcare.hms.appointments.repository;

import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.enums.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistence port for {@link Appointment}.
 *
 * <p>Tenant isolation is enforced by Hibernate {@code tenantFilter} on
 * {@link com.healthcare.hms.common.persistence.TenantOwnedEntity}. Soft-deleted
 * rows are excluded by {@code @SQLRestriction}.
 */
@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {

    Optional<Appointment> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Appointment> findByTenantIdAndAppointmentNumberIgnoreCase(UUID tenantId, String appointmentNumber);

    boolean existsByTenantIdAndAppointmentNumberIgnoreCase(UUID tenantId, String appointmentNumber);

    boolean existsByTenantIdAndAppointmentNumberIgnoreCaseAndIdNot(
            UUID tenantId, String appointmentNumber, UUID id);

    List<Appointment> findByTenantIdAndDoctorIdAndAppointmentDate(
            UUID tenantId, UUID doctorId, LocalDate appointmentDate);

    List<Appointment> findByTenantIdAndPatientIdOrderByAppointmentDateDescStartTimeDesc(
            UUID tenantId, UUID patientId);

    List<Appointment> findByTenantIdAndAppointmentDateAndStatus(
            UUID tenantId, LocalDate appointmentDate, AppointmentStatus status);

    long countByTenantIdAndStatus(UUID tenantId, AppointmentStatus status);

    long countByTenantIdAndDoctorIdAndAppointmentDateAndStatusIn(
            UUID tenantId, UUID doctorId, LocalDate appointmentDate, Collection<AppointmentStatus> statuses);

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.tenantId = :tenantId
              AND a.appointmentDate BETWEEN :fromDate AND :toDate
              AND (:doctorId IS NULL OR a.doctorId = :doctorId)
              AND (:departmentId IS NULL OR a.departmentId = :departmentId)
              AND (:hospitalId IS NULL OR a.hospitalId = :hospitalId)
              AND (:status IS NULL OR a.status = :status)
            ORDER BY a.appointmentDate ASC, a.startTime ASC
            """)
    Page<Appointment> findCalendarEvents(
            @Param("tenantId") UUID tenantId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("doctorId") UUID doctorId,
            @Param("departmentId") UUID departmentId,
            @Param("hospitalId") UUID hospitalId,
            @Param("status") AppointmentStatus status,
            Pageable pageable
    );

    /**
     * Monthly / overview aggregates — single GROUP BY query (no N+1).
     */
    @Query("""
            SELECT a.appointmentDate, a.status, COUNT(a)
            FROM Appointment a
            WHERE a.tenantId = :tenantId
              AND a.appointmentDate BETWEEN :fromDate AND :toDate
              AND (:doctorId IS NULL OR a.doctorId = :doctorId)
              AND (:departmentId IS NULL OR a.departmentId = :departmentId)
              AND (:hospitalId IS NULL OR a.hospitalId = :hospitalId)
              AND (:status IS NULL OR a.status = :status)
            GROUP BY a.appointmentDate, a.status
            ORDER BY a.appointmentDate ASC
            """)
    List<Object[]> aggregateCalendarByDateAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("doctorId") UUID doctorId,
            @Param("departmentId") UUID departmentId,
            @Param("hospitalId") UUID hospitalId,
            @Param("status") AppointmentStatus status
    );

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.tenantId = :tenantId
              AND a.doctorId = :doctorId
              AND a.appointmentDate = :appointmentDate
              AND a.status IN :statuses
              AND a.startTime < :endTime
              AND a.endTime > :startTime
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    List<Appointment> findDoctorSlotConflicts(
            @Param("tenantId") UUID tenantId,
            @Param("doctorId") UUID doctorId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<AppointmentStatus> statuses,
            @Param("excludeId") UUID excludeId
    );

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.tenantId = :tenantId
              AND a.patientId = :patientId
              AND a.appointmentDate = :appointmentDate
              AND a.status IN :statuses
              AND a.startTime < :endTime
              AND a.endTime > :startTime
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    List<Appointment> findPatientSlotConflicts(
            @Param("tenantId") UUID tenantId,
            @Param("patientId") UUID patientId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<AppointmentStatus> statuses,
            @Param("excludeId") UUID excludeId
    );
}
