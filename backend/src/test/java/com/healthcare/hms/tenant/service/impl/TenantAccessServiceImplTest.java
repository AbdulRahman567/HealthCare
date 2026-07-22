package com.healthcare.hms.tenant.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.exception.TenantNotActiveException;
import com.healthcare.hms.tenant.exception.TenantNotFoundException;
import com.healthcare.hms.tenant.repository.TenantRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantAccessServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    private TenantAccessServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TenantAccessServiceImpl(tenantRepository);
    }

    @Test
    void requireActiveTenant_whenActive_returnsTenant() {
        final Tenant tenant = AuthTestData.activeTenant();
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

        assertThat(service.requireActiveTenant(tenant.getId())).isSameAs(tenant);
        assertThat(service.toContext(tenant).tenantId()).isEqualTo(tenant.getId());
    }

    @Test
    void requireActiveTenant_whenSuspended_throws() {
        final Tenant tenant = AuthTestData.activeTenant();
        tenant.setStatus(TenantStatus.SUSPENDED);
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> service.requireActiveTenant(tenant.getId()))
                .isInstanceOf(TenantNotActiveException.class)
                .hasMessageContaining("not operational");
    }

    @Test
    void requireTenant_whenMissing_throws() {
        final UUID id = UUID.randomUUID();
        when(tenantRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireTenant(id))
                .isInstanceOf(TenantNotFoundException.class);
    }
}
