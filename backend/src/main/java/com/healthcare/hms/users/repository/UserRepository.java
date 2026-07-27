package com.healthcare.hms.users.repository;

import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.id = :id
            """)
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.id = :id AND u.tenantId = :tenantId
            """)
    Optional<User> findByIdAndTenantIdWithRolesAndPermissions(
            @Param("id") UUID id,
            @Param("tenantId") UUID tenantId
    );

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE LOWER(u.email) = LOWER(:email)
            """)
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    @Query("""
            SELECT COUNT(u) FROM User u
            JOIN u.roles r
            WHERE u.tenantId = :tenantId
              AND r.type = :roleType
            """)
    long countByTenantIdAndRoleType(
            @Param("tenantId") UUID tenantId,
            @Param("roleType") RoleType roleType
    );

    @Query("""
            SELECT COUNT(DISTINCT u) FROM User u
            JOIN u.roles r
            WHERE u.tenantId = :tenantId
              AND r.type = :roleType
              AND u.status = :status
            """)
    long countByTenantIdAndRoleTypeAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("roleType") RoleType roleType,
            @Param("status") UserStatus status
    );

    long countByTenantIdAndStatus(UUID tenantId, UserStatus status);

    List<User> findByTenantIdAndIdIn(UUID tenantId, Collection<UUID> ids);
}
