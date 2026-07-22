package com.healthcare.hms.tenant.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.tenant.exception.TenantInvalidTransitionException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantLifecycleTest {

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setName("City Hospital");
        tenant.setSlug("city-hospital");
        tenant.setTenantType(TenantType.HOSPITAL);
        tenant.setEmail("admin@city.test");
        tenant.setStatus(TenantStatus.PENDING);
    }

    @Test
    void activate_fromPending_becomesActiveAndOperational() {
        tenant.activate();

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.isOperational()).isTrue();
    }

    @Test
    void suspend_fromActive_blocksOperations() {
        tenant.activate();
        tenant.suspend();

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
        assertThat(tenant.isOperational()).isFalse();
    }

    @Test
    void suspend_fromPending_rejectsTransition() {
        assertThatThrownBy(tenant::suspend)
                .isInstanceOf(TenantInvalidTransitionException.class)
                .hasMessageContaining("Cannot transition");
    }

    @Test
    void activate_fromSuspended_restoresOperationalAccess() {
        tenant.activate();
        tenant.suspend();
        tenant.activate();

        assertThat(tenant.isOperational()).isTrue();
    }

    @Test
    void deactivate_fromActive_marksInactive() {
        tenant.activate();
        tenant.deactivate();

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.INACTIVE);
        assertThat(tenant.isOperational()).isFalse();
    }

    @Test
    void softDelete_marksDeletedAndNotOperational() {
        tenant.activate();
        final UUID actor = UUID.randomUUID();

        tenant.softDelete(actor);

        assertThat(tenant.isDeleted()).isTrue();
        assertThat(tenant.getDeletedBy()).isEqualTo(actor);
        assertThat(tenant.getDeletedAt()).isNotNull();
        assertThat(tenant.isOperational()).isFalse();
    }

    @Test
    void activate_fromActive_rejectsTransition() {
        tenant.activate();

        assertThatThrownBy(tenant::activate)
                .isInstanceOf(TenantInvalidTransitionException.class);
    }
}
