package com.healthcare.hms.patients.repository;

import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.enums.PatientStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Persistence port for {@link Patient}.
 *
 * <p>Tenant isolation is enforced by Hibernate {@code tenantFilter} on
 * {@link com.healthcare.hms.common.persistence.TenantOwnedEntity}. Soft-deleted
 * rows are excluded by {@code @SQLRestriction}. Explicit {@code tenantId}
 * methods remain for defensive lookups and uniqueness checks used by future
 * services.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {

    Optional<Patient> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Patient> findByTenantIdAndMrnIgnoreCase(UUID tenantId, String mrn);

    boolean existsByTenantIdAndMrnIgnoreCase(UUID tenantId, String mrn);

    boolean existsByTenantIdAndMrnIgnoreCaseAndIdNot(UUID tenantId, String mrn, UUID id);

    boolean existsByTenantIdAndNationalIdIgnoreCase(UUID tenantId, String nationalId);

    boolean existsByTenantIdAndNationalIdIgnoreCaseAndIdNot(UUID tenantId, String nationalId, UUID id);

    boolean existsByTenantIdAndPhone(UUID tenantId, String phone);

    long countByTenantIdAndStatus(UUID tenantId, PatientStatus status);
}
