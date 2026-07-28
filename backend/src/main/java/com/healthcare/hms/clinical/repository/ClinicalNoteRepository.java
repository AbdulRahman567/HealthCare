package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.ClinicalNote;
import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicalNoteRepository
        extends JpaRepository<ClinicalNote, UUID>, JpaSpecificationExecutor<ClinicalNote> {

    Optional<ClinicalNote> findByIdAndTenantIdAndConsultationId(UUID id, UUID tenantId, UUID consultationId);

    List<ClinicalNote> findByTenantIdAndConsultationIdOrderByRecordedAtAsc(UUID tenantId, UUID consultationId);

    List<ClinicalNote> findByTenantIdAndConsultationIdAndNoteTypeOrderByRecordedAtAsc(
            UUID tenantId, UUID consultationId, ClinicalNoteType noteType);

    List<ClinicalNote> findByTenantIdAndPatientIdOrderByRecordedAtDesc(UUID tenantId, UUID patientId);

    long countByTenantIdAndConsultationId(UUID tenantId, UUID consultationId);
}
