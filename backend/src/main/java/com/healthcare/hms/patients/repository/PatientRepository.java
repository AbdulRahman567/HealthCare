package com.healthcare.hms.patients.repository;

import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.enums.PatientStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p FROM Patient p
            WHERE p.id = :id AND p.tenantId = :tenantId
            """)
    Optional<Patient> findByIdAndTenantIdForUpdate(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    Optional<Patient> findByTenantIdAndMrnIgnoreCase(UUID tenantId, String mrn);

    boolean existsByTenantIdAndMrnIgnoreCase(UUID tenantId, String mrn);

    boolean existsByTenantIdAndMrnIgnoreCaseAndIdNot(UUID tenantId, String mrn, UUID id);

    boolean existsByTenantIdAndNationalIdIgnoreCase(UUID tenantId, String nationalId);

    boolean existsByTenantIdAndNationalIdIgnoreCaseAndIdNot(UUID tenantId, String nationalId, UUID id);

    boolean existsByTenantIdAndPhone(UUID tenantId, String phone);

    long countByTenantIdAndStatus(UUID tenantId, PatientStatus status);

    List<Patient> findByTenantIdAndIdIn(UUID tenantId, Collection<UUID> ids);
}
