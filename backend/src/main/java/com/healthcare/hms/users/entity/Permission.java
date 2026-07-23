package com.healthcare.hms.users.entity;

import com.healthcare.hms.common.persistence.BaseEntity;
import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import com.healthcare.hms.users.rbac.PermissionNaming;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.SQLRestriction;

/**
 * Global permission catalog entry (platform-scoped, not tenant-owned).
 *
 * <p>Permissions are shared across tenants. Tenant isolation is enforced by which
 * {@link Role} rows (tenant-scoped or platform) hold grants via {@code role_permissions}.
 *
 * <p>Canonical {@code code} format: {@code {GROUP}_{ACTION}} per {@link PermissionNaming}.
 */
@Entity
@Table(
        name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permissions_code", columnNames = {"code"}),
                @UniqueConstraint(
                        name = "uk_permissions_group_action",
                        columnNames = {"permission_group", "action"}
                )
        },
        indexes = {
                @Index(name = "idx_permissions_group", columnList = "permission_group"),
                @Index(name = "idx_permissions_action", columnList = "action"),
                @Index(name = "idx_permissions_group_action", columnList = "permission_group, action"),
                @Index(name = "idx_permissions_system", columnList = "system_permission"),
                @Index(name = "idx_permissions_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Permission extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_group", nullable = false, length = 80)
    private PermissionGroup permissionGroup;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private PermissionAction action;

    /**
     * Platform-seeded catalog rows are immutable from application admin APIs.
     */
    @Column(name = "system_permission", nullable = false)
    private boolean systemPermission = true;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    @PreUpdate
    void syncCodeWithGroupAndAction() {
        if (permissionGroup != null && action != null) {
            final String expected = PermissionNaming.code(permissionGroup, action);
            if (code == null || code.isBlank()) {
                code = expected;
            } else if (!expected.equals(code)) {
                throw new IllegalStateException(
                        "Permission code '" + code + "' does not match group/action '" + expected + "'"
                );
            }
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public PermissionGroup getPermissionGroup() {
        return permissionGroup;
    }

    public void setPermissionGroup(final PermissionGroup permissionGroup) {
        this.permissionGroup = permissionGroup;
    }

    public PermissionAction getAction() {
        return action;
    }

    public void setAction(final PermissionAction action) {
        this.action = action;
    }

    public boolean isSystemPermission() {
        return systemPermission;
    }

    public void setSystemPermission(final boolean systemPermission) {
        this.systemPermission = systemPermission;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }
}
