package com.healthcare.hms.patients.history.repository;

import com.healthcare.hms.patients.history.entity.FamilyHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyHistoryRepository extends JpaRepository<FamilyHistory, UUID> {

    Optional<FamilyHistory> findByIdAndTenantIdAndPatientId(UUID id, UUID tenantId, UUID patientId);

    List<FamilyHistory> findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(UUID tenantId, UUID patientId);
}
