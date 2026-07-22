package com.healthcare.hms.tenant.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.tenant.exception.TenantRequiredException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextHolderTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void setAndRequire_roundTripsTenantId() {
        final UUID tenantId = UUID.randomUUID();
        TenantContextHolder.set(new TenantContext(
                tenantId,
                "city-hospital",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        assertThat(TenantContextHolder.requireTenantId()).isEqualTo(tenantId);
        assertThat(TenantContextHolder.get()).isPresent();
        assertThat(TenantContextHolder.get().orElseThrow().isOperational()).isTrue();
        assertThat(TenantContextHolder.isPresent()).isTrue();
    }

    @Test
    void requireTenantId_whenEmpty_throws() {
        assertThatThrownBy(TenantContextHolder::requireTenantId)
                .isInstanceOf(TenantRequiredException.class);
    }

    @Test
    void clear_removesBinding() {
        TenantContextHolder.set(new TenantContext(
                UUID.randomUUID(),
                "x",
                TenantType.CLINIC,
                TenantStatus.ACTIVE
        ));
        TenantContextHolder.clear();

        assertThat(TenantContextHolder.get()).isEmpty();
        assertThat(TenantContextHolder.isPresent()).isFalse();
    }

    @Test
    void setNull_clearsContext() {
        TenantContextHolder.set(new TenantContext(
                UUID.randomUUID(),
                "x",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));
        TenantContextHolder.set(null);

        assertThat(TenantContextHolder.get()).isEmpty();
    }
}
