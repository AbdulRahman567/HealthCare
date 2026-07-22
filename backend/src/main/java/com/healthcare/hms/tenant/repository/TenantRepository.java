package com.healthcare.hms.tenant.repository;

import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.enums.TenantStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistence port for the {@link Tenant} aggregate.
 * Soft-deleted rows are excluded by the entity {@code @SQLRestriction}.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySlugIgnoreCase(String slug);

    Optional<Tenant> findByEmailIgnoreCase(String email);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Tenant> findByIdAndStatus(UUID id, TenantStatus status);

    @Query("""
            SELECT t FROM Tenant t
            WHERE t.id = :id
              AND t.status = com.healthcare.hms.tenant.enums.TenantStatus.ACTIVE
            """)
    Optional<Tenant> findActiveById(@Param("id") UUID id);
}
