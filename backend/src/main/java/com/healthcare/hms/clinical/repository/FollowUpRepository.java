package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.FollowUp;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, UUID>, JpaSpecificationExecutor<FollowUp> {

    Optional<FollowUp> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<FollowUp> findByIdAndTenantIdAndConsultationId(UUID id, UUID tenantId, UUID consultationId);

    List<FollowUp> findByTenantIdAndConsultationIdOrderByScheduledDateAsc(UUID tenantId, UUID consultationId);

    List<FollowUp> findByTenantIdAndPatientIdOrderByScheduledDateDesc(UUID tenantId, UUID patientId);

    Page<FollowUp> findByTenantIdAndDoctorIdAndStatus(
            UUID tenantId, UUID doctorId, FollowUpStatus status, Pageable pageable);

    List<FollowUp> findByTenantIdAndDoctorIdAndScheduledDateBetweenAndStatusIn(
            UUID tenantId,
            UUID doctorId,
            LocalDate fromDate,
            LocalDate toDate,
            List<FollowUpStatus> statuses);

    List<FollowUp> findByTenantIdAndPatientIdAndStatusOrderByScheduledDateAsc(
            UUID tenantId, UUID patientId, FollowUpStatus status);

    Optional<FollowUp> findByTenantIdAndFollowUpAppointmentId(UUID tenantId, UUID followUpAppointmentId);

    long countByTenantIdAndConsultationId(UUID tenantId, UUID consultationId);
}
