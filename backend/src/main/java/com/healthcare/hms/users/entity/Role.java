package com.healthcare.hms.users.entity;

import com.healthcare.hms.common.persistence.TenantAwareEntity;
import com.healthcare.hms.common.persistence.TenantPersistence;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.rbac.RoleHierarchy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLRestriction;

/**
 * RBAC role — either a platform system template ({@code tenant_id IS NULL}) or a
 * tenant-scoped operational role.
 *
 * <p>Hierarchy is structural ({@link #hierarchyLevel}, {@link #parentRole}); effective
 * access is always the explicit permission set on this role.
 */
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
                @Index(name = "idx_roles_parent_role_id", columnList = "parent_role_id"),
                @Index(name = "idx_roles_hierarchy_level", columnList = "hierarchy_level"),
                @Index(name = "idx_roles_tenant_hierarchy", columnList = "tenant_id, hierarchy_level"),
                @Index(name = "idx_roles_system_role", columnList = "system_role"),
                @Index(name = "idx_roles_assignable", columnList = "assignable"),
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

    /**
     * Platform catalog template when {@code true} and {@code tenant_id IS NULL}.
     * Tenant-provisioned copies of default roles use {@code false}.
     */
    @Column(name = "system_role", nullable = false)
    private boolean systemRole = false;

    /**
     * Privilege rank — lower value means higher privilege. Defaults from {@link RoleHierarchy}.
     */
    @Column(name = "hierarchy_level", nullable = false)
    private int hierarchyLevel;

    /**
     * Whether hospital administrators may assign this role to users.
     */
    @Column(name = "assignable", nullable = false)
    private boolean assignable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id")
    private Role parentRole;

    @OneToMany(mappedBy = "parentRole", fetch = FetchType.LAZY)
    private Set<Role> childRoles = new HashSet<>();

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

    /**
     * Applies catalog hierarchy level and assignable flag from {@link RoleHierarchy}.
     * Call when creating roles from the default system/tenant catalogs.
     */
    public void applyCatalogHierarchy() {
        if (type == null) {
            return;
        }
        this.hierarchyLevel = RoleHierarchy.levelOf(type);
        this.assignable = RoleHierarchy.isAssignable(type);
    }

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

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(final int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public boolean isAssignable() {
        return assignable;
    }

    public void setAssignable(final boolean assignable) {
        this.assignable = assignable;
    }

    public Role getParentRole() {
        return parentRole;
    }

    public void setParentRole(final Role parentRole) {
        this.parentRole = parentRole;
    }

    public Set<Role> getChildRoles() {
        return childRoles;
    }

    public void setChildRoles(final Set<Role> childRoles) {
        this.childRoles = childRoles;
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
