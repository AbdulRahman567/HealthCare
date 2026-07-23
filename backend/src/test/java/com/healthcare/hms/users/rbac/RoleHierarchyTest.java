package com.healthcare.hms.users.rbac;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.users.enums.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RoleHierarchy")
class RoleHierarchyTest {

    @Test
    @DisplayName("orders privilege levels with Super Admin at the top")
    void levelOf_ordersPrivilege() {
        assertThat(RoleHierarchy.levelOf(RoleType.SUPER_ADMIN))
                .isLessThan(RoleHierarchy.levelOf(RoleType.HOSPITAL_ADMIN));
        assertThat(RoleHierarchy.levelOf(RoleType.HOSPITAL_ADMIN))
                .isLessThan(RoleHierarchy.levelOf(RoleType.DOCTOR));
        assertThat(RoleHierarchy.levelOf(RoleType.DOCTOR))
                .isLessThan(RoleHierarchy.levelOf(RoleType.NURSE));
        assertThat(RoleHierarchy.levelOf(RoleType.NURSE))
                .isEqualTo(RoleHierarchy.levelOf(RoleType.ACCOUNTANT));
        assertThat(RoleHierarchy.parentOf(RoleType.ACCOUNTANT)).contains(RoleType.HOSPITAL_ADMIN);
        assertThat(RoleHierarchy.isAssignable(RoleType.ACCOUNTANT)).isTrue();
        assertThat(RoleHierarchy.isStrictlyHigher(RoleType.HOSPITAL_ADMIN, RoleType.PATIENT)).isTrue();
    }

    @Test
    @DisplayName("defines platform parent chain and tenant root")
    void parentAndTenantRoot() {
        assertThat(RoleHierarchy.parentOf(RoleType.SUPER_ADMIN)).isEmpty();
        assertThat(RoleHierarchy.parentOf(RoleType.HOSPITAL_ADMIN)).contains(RoleType.SUPER_ADMIN);
        assertThat(RoleHierarchy.parentOf(RoleType.DOCTOR)).contains(RoleType.HOSPITAL_ADMIN);
        assertThat(RoleHierarchy.isTenantRoot(RoleType.HOSPITAL_ADMIN)).isTrue();
        assertThat(RoleHierarchy.isPlatformOnly(RoleType.SUPER_ADMIN)).isTrue();
        assertThat(RoleHierarchy.isAssignable(RoleType.DOCTOR)).isTrue();
        assertThat(RoleHierarchy.isAssignable(RoleType.SUPER_ADMIN)).isFalse();
        assertThat(RoleHierarchy.isAssignable(RoleType.PATIENT)).isFalse();
    }
}
