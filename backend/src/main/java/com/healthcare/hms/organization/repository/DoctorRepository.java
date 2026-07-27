package com.healthcare.hms.organization.repository;

import com.healthcare.hms.organization.entity.Doctor;
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

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID>, JpaSpecificationExecutor<Doctor> {

    Optional<Doctor> findByIdAndTenantId(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT d FROM Doctor d
            WHERE d.id = :id AND d.tenantId = :tenantId
            """)
    Optional<Doctor> findByIdAndTenantIdForUpdate(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    boolean existsByTenantIdAndEmployeeCodeIgnoreCase(UUID tenantId, String employeeCode);

    boolean existsByTenantIdAndEmployeeCodeIgnoreCaseAndIdNot(UUID tenantId, String employeeCode, UUID id);

    boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

    boolean existsByTenantIdAndUserIdAndIdNot(UUID tenantId, UUID userId, UUID id);

    boolean existsByTenantIdAndLicenseNumberIgnoreCase(UUID tenantId, String licenseNumber);

    boolean existsByTenantIdAndLicenseNumberIgnoreCaseAndIdNot(UUID tenantId, String licenseNumber, UUID id);

    boolean existsByUserId(UUID userId);

    boolean existsByDepartmentId(UUID departmentId);

    java.util.Optional<Doctor> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    List<Doctor> findByTenantIdAndIdIn(UUID tenantId, Collection<UUID> ids);
}
