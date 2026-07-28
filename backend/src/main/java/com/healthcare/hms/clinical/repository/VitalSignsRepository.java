package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.VitalSigns;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, UUID>, JpaSpecificationExecutor<VitalSigns> {

    Optional<VitalSigns> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<VitalSigns> findByIdAndTenantIdAndConsultationId(UUID id, UUID tenantId, UUID consultationId);

    List<VitalSigns> findByTenantIdAndConsultationIdOrderByRecordedAtAsc(UUID tenantId, UUID consultationId);

    List<VitalSigns> findByTenantIdAndPatientIdOrderByRecordedAtDesc(UUID tenantId, UUID patientId);

    Page<VitalSigns> findByTenantIdAndPatientId(UUID tenantId, UUID patientId, Pageable pageable);

    Page<VitalSigns> findByTenantIdAndPatientIdAndRecordedAtBetween(
            UUID tenantId,
            UUID patientId,
            Instant fromRecordedAt,
            Instant toRecordedAt,
            Pageable pageable
    );

    Optional<VitalSigns> findFirstByTenantIdAndConsultationIdOrderByRecordedAtDesc(
            UUID tenantId, UUID consultationId);

    long countByTenantIdAndConsultationId(UUID tenantId, UUID consultationId);
}
