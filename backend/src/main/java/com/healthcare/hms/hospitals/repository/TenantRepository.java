package com.healthcare.hms.hospitals.repository;

import com.healthcare.hms.hospitals.entity.Tenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySlugIgnoreCase(String slug);

    Optional<Tenant> findByEmailIgnoreCase(String email);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsByEmailIgnoreCase(String email);
}
