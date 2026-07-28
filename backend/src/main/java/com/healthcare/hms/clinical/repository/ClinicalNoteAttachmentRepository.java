package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.ClinicalNoteAttachment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicalNoteAttachmentRepository extends JpaRepository<ClinicalNoteAttachment, UUID> {

    Optional<ClinicalNoteAttachment> findByIdAndTenantIdAndClinicalNoteId(
            UUID id, UUID tenantId, UUID clinicalNoteId);

    List<ClinicalNoteAttachment> findByTenantIdAndClinicalNoteIdOrderByCreatedAtAsc(
            UUID tenantId, UUID clinicalNoteId);

    List<ClinicalNoteAttachment> findByTenantIdAndConsultationIdOrderByCreatedAtDesc(
            UUID tenantId, UUID consultationId);

    long countByTenantIdAndClinicalNoteId(UUID tenantId, UUID clinicalNoteId);
}
