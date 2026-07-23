package com.healthcare.hms.users.repository;

import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    List<Permission> findByCodeIn(Collection<String> codes);

    List<Permission> findByPermissionGroup(PermissionGroup permissionGroup);

    Optional<Permission> findByPermissionGroupAndAction(
            PermissionGroup permissionGroup,
            PermissionAction action
    );

    List<Permission> findBySystemPermissionTrue();

    boolean existsByCode(String code);

    boolean existsByPermissionGroupAndAction(PermissionGroup permissionGroup, PermissionAction action);
}
