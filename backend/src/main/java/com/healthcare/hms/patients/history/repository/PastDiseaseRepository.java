package com.healthcare.hms.patients.history.repository;

import com.healthcare.hms.patients.history.entity.PastDisease;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PastDiseaseRepository extends JpaRepository<PastDisease, UUID> {

    Optional<PastDisease> findByIdAndTenantIdAndPatientId(UUID id, UUID tenantId, UUID patientId);

    List<PastDisease> findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(UUID tenantId, UUID patientId);
}
