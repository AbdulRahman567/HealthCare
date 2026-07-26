package com.healthcare.hms.patients.history.repository;

import com.healthcare.hms.patients.history.entity.SurgeryHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurgeryHistoryRepository extends JpaRepository<SurgeryHistory, UUID> {

    Optional<SurgeryHistory> findByIdAndTenantIdAndPatientId(UUID id, UUID tenantId, UUID patientId);

    List<SurgeryHistory> findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(UUID tenantId, UUID patientId);
}
