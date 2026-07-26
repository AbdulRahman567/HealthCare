package com.healthcare.hms.patients.history.repository;

import com.healthcare.hms.patients.history.entity.MedicalHistory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, UUID> {

    Optional<MedicalHistory> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<MedicalHistory> findByTenantIdAndPatientId(UUID tenantId, UUID patientId);

    boolean existsByTenantIdAndPatientId(UUID tenantId, UUID patientId);
}
