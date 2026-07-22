package com.healthcare.hms.users.entity;

import com.healthcare.hms.common.persistence.TenantAwareEntity;
import com.healthcare.hms.common.persistence.TenantPersistence;
import com.healthcare.hms.users.enums.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roles_tenant_type", columnNames = {"tenant_id", "type"}),
                @UniqueConstraint(name = "uk_roles_tenant_name", columnNames = {"tenant_id", "name"})
        },
        indexes = {
                @Index(name = "idx_roles_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_roles_type", columnList = "type"),
                @Index(name = "idx_roles_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
@Filter(name = TenantPersistence.FILTER_NAME, condition = TenantPersistence.CONDITION_INCLUDE_PLATFORM_SYSTEM)
public class Role extends TenantAwareEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private RoleType type;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "system_role", nullable = false)
    private boolean systemRole = false;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_role_permissions_role_permission",
                    columnNames = {"role_id", "permission_id"}
            ),
            indexes = {
                    @Index(name = "idx_role_permissions_role_id", columnList = "role_id"),
                    @Index(name = "idx_role_permissions_permission_id", columnList = "permission_id")
            }
    )
    private Set<Permission> permissions = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public RoleType getType() {
        return type;
    }

    public void setType(final RoleType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public void setSystemRole(final boolean systemRole) {
        this.systemRole = systemRole;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(final Set<User> users) {
        this.users = users;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(final Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(final Permission permission) {
        permissions.add(permission);
        permission.getRoles().add(this);
    }

    public void removePermission(final Permission permission) {
        permissions.remove(permission);
        permission.getRoles().remove(this);
    }
}
