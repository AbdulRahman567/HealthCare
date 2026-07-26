package com.healthcare.hms.patients.history.repository;

import com.healthcare.hms.patients.history.entity.ChronicCondition;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChronicConditionRepository extends JpaRepository<ChronicCondition, UUID> {

    Optional<ChronicCondition> findByIdAndTenantIdAndPatientId(UUID id, UUID tenantId, UUID patientId);

    List<ChronicCondition> findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(UUID tenantId, UUID patientId);
}
