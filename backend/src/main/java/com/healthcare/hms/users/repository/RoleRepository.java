package com.healthcare.hms.users.repository;

import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.enums.RoleType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByTenantIdAndType(UUID tenantId, RoleType type);

    Optional<Role> findByTenantIdIsNullAndType(RoleType type);

    @Query("""
            SELECT DISTINCT r FROM Role r
            LEFT JOIN FETCH r.permissions
            WHERE r.tenantId IS NULL
              AND r.type = :type
            """)
    Optional<Role> findSystemRoleWithPermissions(@Param("type") RoleType type);

    List<Role> findByTenantId(UUID tenantId);

    List<Role> findByTenantIdOrderByHierarchyLevelAsc(UUID tenantId);

    List<Role> findByParentRoleId(UUID parentRoleId);

    List<Role> findByTenantIdAndAssignableTrue(UUID tenantId);

    List<Role> findByTenantIdAndHierarchyLevelGreaterThan(UUID tenantId, int hierarchyLevel);

    @Query("""
            SELECT DISTINCT r FROM Role r
            LEFT JOIN FETCH r.permissions
            WHERE r.id = :id
            """)
    Optional<Role> findByIdWithPermissions(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT r FROM Role r
            LEFT JOIN FETCH r.permissions
            WHERE r.tenantId = :tenantId
            ORDER BY r.hierarchyLevel ASC
            """)
    List<Role> findByTenantIdWithPermissions(@Param("tenantId") UUID tenantId);

    boolean existsByTenantIdAndType(UUID tenantId, RoleType type);
}
