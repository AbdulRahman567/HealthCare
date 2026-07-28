package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.enums.ConsultationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Persistence port for {@link Consultation}.
 *
 * <p>Tenant isolation is enforced by Hibernate {@code tenantFilter} on
 * {@link com.healthcare.hms.common.persistence.TenantOwnedEntity}. Soft-deleted
 * rows are excluded by {@code @SQLRestriction}.
 */
@Repository
public interface ConsultationRepository
        extends JpaRepository<Consultation, UUID>, JpaSpecificationExecutor<Consultation> {

    Optional<Consultation> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Consultation> findByTenantIdAndConsultationNumberIgnoreCase(UUID tenantId, String consultationNumber);

    boolean existsByTenantIdAndConsultationNumberIgnoreCase(UUID tenantId, String consultationNumber);

    boolean existsByTenantIdAndConsultationNumberIgnoreCaseAndIdNot(
            UUID tenantId, String consultationNumber, UUID id);

    Optional<Consultation> findByTenantIdAndAppointmentId(UUID tenantId, UUID appointmentId);

    List<Consultation> findByTenantIdAndAppointmentIdIn(UUID tenantId, java.util.Collection<UUID> appointmentIds);

    boolean existsByTenantIdAndAppointmentIdAndIdNot(UUID tenantId, UUID appointmentId, UUID id);

    List<Consultation> findByTenantIdAndPatientIdOrderByConsultationDateDescStartedAtDesc(
            UUID tenantId, UUID patientId);

    Page<Consultation> findByTenantIdAndPatientId(UUID tenantId, UUID patientId, Pageable pageable);

    Page<Consultation> findByTenantIdAndDoctorId(UUID tenantId, UUID doctorId, Pageable pageable);

    List<Consultation> findByTenantIdAndDoctorIdAndConsultationDate(
            UUID tenantId, UUID doctorId, LocalDate consultationDate);

    List<Consultation> findByTenantIdAndPatientIdAndConsultationDateBetweenOrderByConsultationDateDesc(
            UUID tenantId, UUID patientId, LocalDate fromDate, LocalDate toDate);

    long countByTenantIdAndStatus(UUID tenantId, ConsultationStatus status);

    long countByTenantIdAndDoctorIdAndConsultationDateAndStatus(
            UUID tenantId, UUID doctorId, LocalDate consultationDate, ConsultationStatus status);

    boolean existsByTenantIdAndDoctorIdAndStatusIn(
            UUID tenantId, UUID doctorId, java.util.Collection<ConsultationStatus> statuses);

    boolean existsByTenantIdAndDoctorIdAndStatusInAndIdNot(
            UUID tenantId,
            UUID doctorId,
            java.util.Collection<ConsultationStatus> statuses,
            UUID excludeId);
}
