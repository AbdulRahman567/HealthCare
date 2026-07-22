package com.healthcare.hms.hospitals.repository;

import com.healthcare.hms.hospitals.entity.Hospital;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, UUID> {

    Optional<Hospital> findByTenantIdAndDefaultHospitalTrue(UUID tenantId);

    boolean existsByTenantId(UUID tenantId);

    boolean existsByTenantIdAndNameIgnoreCaseAndIdNot(UUID tenantId, String name, UUID id);
}
