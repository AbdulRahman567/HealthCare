package com.healthcare.hms.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.tenant.exception.TenantMismatchException;
import com.healthcare.hms.users.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TenantEntityListenerTest {

    private final TenantEntityListener listener = new TenantEntityListener();

    @AfterEach
    void clearContext() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("PrePersist stamps tenant_id from TenantContextHolder when unset")
    void prePersist_stampsTenantFromContext() {
        final UUID tenantId = UUID.randomUUID();
        bind(tenantId);
        final User user = new User();

        listener.onPrePersist(user);

        assertThat(user.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("PrePersist keeps explicit tenant_id when no context is bound")
    void prePersist_keepsExplicitTenantWithoutContext() {
        final UUID tenantId = UUID.randomUUID();
        final User user = new User();
        user.setTenantId(tenantId);

        listener.onPrePersist(user);

        assertThat(user.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("PrePersist rejects entity tenant that conflicts with request context")
    void prePersist_rejectsMismatchedTenant() {
        bind(UUID.randomUUID());
        final User user = new User();
        user.setTenantId(UUID.randomUUID());

        assertThatThrownBy(() -> listener.onPrePersist(user))
                .isInstanceOf(TenantMismatchException.class)
                .extracting(ex -> ((TenantMismatchException) ex).getErrorCode())
                .isEqualTo("TENANT_PERSISTENCE_MISMATCH");
    }

    @Test
    @DisplayName("PreUpdate rejects mutating an entity owned by another tenant")
    void preUpdate_rejectsForeignTenantEntity() {
        bind(UUID.randomUUID());
        final User user = new User();
        user.setTenantId(UUID.randomUUID());

        assertThatThrownBy(() -> listener.onPreUpdate(user))
                .isInstanceOf(TenantMismatchException.class);
    }

    @Test
    @DisplayName("PreUpdate rejects tenant-scoped mutation of platform rows")
    void preUpdate_rejectsPlatformRowUnderTenantContext() {
        bind(UUID.randomUUID());
        final User user = new User();
        user.setTenantId(null);

        assertThatThrownBy(() -> listener.onPreUpdate(user))
                .isInstanceOf(TenantMismatchException.class)
                .extracting(ex -> ((TenantMismatchException) ex).getErrorCode())
                .isEqualTo("TENANT_PLATFORM_ROW_MUTATION");
    }

    private static void bind(final UUID tenantId) {
        TenantContextHolder.set(new TenantContext(
                tenantId,
                "test-hospital",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));
    }
}
