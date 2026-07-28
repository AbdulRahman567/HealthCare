package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.Diagnosis;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import com.healthcare.hms.clinical.enums.DiagnosisType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, UUID>, JpaSpecificationExecutor<Diagnosis> {

    Optional<Diagnosis> findByIdAndTenantIdAndConsultationId(UUID id, UUID tenantId, UUID consultationId);

    List<Diagnosis> findByTenantIdAndConsultationIdOrderBySequenceNumberAsc(UUID tenantId, UUID consultationId);

    List<Diagnosis> findByTenantIdAndPatientIdOrderByDiagnosedAtDesc(UUID tenantId, UUID patientId);

    List<Diagnosis> findByTenantIdAndPatientIdAndDiagnosisStatusOrderByDiagnosedAtDesc(
            UUID tenantId, UUID patientId, DiagnosisStatus diagnosisStatus);

    Optional<Diagnosis> findByTenantIdAndConsultationIdAndDiagnosisType(
            UUID tenantId, UUID consultationId, DiagnosisType diagnosisType);

    boolean existsByTenantIdAndConsultationIdAndDiagnosisType(
            UUID tenantId, UUID consultationId, DiagnosisType diagnosisType);

    boolean existsByTenantIdAndConsultationIdAndDiagnosisTypeAndIdNot(
            UUID tenantId, UUID consultationId, DiagnosisType diagnosisType, UUID id);

    long countByTenantIdAndConsultationId(UUID tenantId, UUID consultationId);
}
