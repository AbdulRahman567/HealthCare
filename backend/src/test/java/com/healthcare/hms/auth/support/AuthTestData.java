package com.healthcare.hms.auth.support;

import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.users.entity.Permission;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import com.healthcare.hms.users.enums.RoleType;
import com.healthcare.hms.users.enums.UserStatus;
import java.util.UUID;

/**
 * Shared factories for authentication unit and integration tests.
 */
public final class AuthTestData {

    public static final String STRONG_PASSWORD = "StrongPass1!ab";
    public static final String STRONG_PASSWORD_ALT = "StrongPass2!cd";

    private AuthTestData() {
    }

    public static UUID tenantId() {
        return UUID.fromString("11111111-1111-4111-8111-111111111111");
    }

    public static UUID userId() {
        return UUID.fromString("22222222-2222-4222-8222-222222222222");
    }

    public static User activeVerifiedUser(final String passwordHash) {
        final User user = new User();
        user.setId(userId());
        user.setTenantId(tenantId());
        user.setFirstName("Jane");
        user.setLastName("Admin");
        user.setEmail("admin@hospital.test");
        user.setPasswordHash(passwordHash);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setTokenVersion(0L);
        return user;
    }

    public static Role hospitalAdminRole() {
        final Role role = new Role();
        role.setId(UUID.fromString("b1000000-0000-4000-8000-000000000002"));
        role.setName("Hospital Admin");
        role.setType(RoleType.HOSPITAL_ADMIN);
        role.setSystemRole(true);
        role.applyCatalogHierarchy();
        final Permission permission = new Permission();
        permission.setId(UUID.fromString("a1000000-0000-4000-8000-000000000007"));
        permission.setCode("HOSPITAL_READ");
        permission.setName("Read hospitals");
        permission.setPermissionGroup(PermissionGroup.HOSPITAL);
        permission.setAction(PermissionAction.READ);
        role.getPermissions().add(permission);
        return role;
    }

    public static Tenant activeTenant() {
        final Tenant tenant = new Tenant();
        tenant.setId(tenantId());
        tenant.setName("Test Hospital");
        tenant.setSlug("test-hospital");
        tenant.setTenantType(TenantType.HOSPITAL);
        tenant.setEmail("hospital@hospital.test");
        tenant.setSubscriptionPlan(SubscriptionPlan.BASIC);
        tenant.setStatus(TenantStatus.ACTIVE);
        return tenant;
    }
}
