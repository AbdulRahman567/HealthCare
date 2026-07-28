package com.healthcare.hms.prescriptions.repository;

import com.healthcare.hms.prescriptions.entity.Prescription;
import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository
        extends JpaRepository<Prescription, UUID>, JpaSpecificationExecutor<Prescription> {

    Optional<Prescription> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Prescription> findByTenantIdAndPrescriptionNumberIgnoreCase(UUID tenantId, String prescriptionNumber);

    boolean existsByTenantIdAndPrescriptionNumberIgnoreCase(UUID tenantId, String prescriptionNumber);

    List<Prescription> findByTenantIdAndConsultationIdOrderByPrescriptionDateDescCreatedAtDesc(
            UUID tenantId, UUID consultationId);

    List<Prescription> findByTenantIdAndPatientIdOrderByPrescriptionDateDescCreatedAtDesc(
            UUID tenantId, UUID patientId);

    List<Prescription> findByTenantIdAndPatientIdAndStatusOrderByPrescriptionDateDesc(
            UUID tenantId, UUID patientId, PrescriptionStatus status);

    long countByTenantIdAndConsultationId(UUID tenantId, UUID consultationId);
}
