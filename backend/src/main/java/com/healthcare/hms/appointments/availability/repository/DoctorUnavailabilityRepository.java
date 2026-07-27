package com.healthcare.hms.appointments.availability.repository;

import com.healthcare.hms.appointments.availability.entity.DoctorUnavailability;
import com.healthcare.hms.appointments.availability.enums.UnavailabilityType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorUnavailabilityRepository extends JpaRepository<DoctorUnavailability, UUID> {

    Optional<DoctorUnavailability> findByIdAndTenantIdAndDoctorId(UUID id, UUID tenantId, UUID doctorId);

    List<DoctorUnavailability> findByTenantIdAndDoctorIdOrderByStartDateDesc(UUID tenantId, UUID doctorId);

    List<DoctorUnavailability> findByTenantIdAndDoctorIdAndUnavailabilityTypeOrderByStartDateDesc(
            UUID tenantId, UUID doctorId, UnavailabilityType type);

    @Query("""
            SELECT u FROM DoctorUnavailability u
            WHERE u.tenantId = :tenantId
              AND u.doctorId = :doctorId
              AND u.startDate <= :endDate
              AND u.endDate >= :startDate
              AND (:excludeId IS NULL OR u.id <> :excludeId)
            """)
    List<DoctorUnavailability> findOverlapping(
            @Param("tenantId") UUID tenantId,
            @Param("doctorId") UUID doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") UUID excludeId
    );
}
