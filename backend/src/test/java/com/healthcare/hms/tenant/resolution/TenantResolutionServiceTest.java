package com.healthcare.hms.tenant.resolution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.healthcare.hms.security.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantResolutionServiceTest {

    @Mock
    private HttpServletRequest request;

    private HeaderTenantResolver headerTenantResolver;
    private SubdomainTenantResolver subdomainTenantResolver;
    private TenantResolutionService service;

    @BeforeEach
    void setUp() {
        headerTenantResolver = new HeaderTenantResolver();
        subdomainTenantResolver = new SubdomainTenantResolver(false);
        service = new TenantResolutionService(List.of(subdomainTenantResolver, headerTenantResolver));
    }

    @Test
    void resolveTenantId_prefersHeaderOverDisabledSubdomain() {
        final UUID tenantId = UUID.randomUUID();
        when(request.getHeaders(SecurityConstants.TENANT_HEADER))
                .thenReturn(Collections.enumeration(List.of(tenantId.toString())));

        assertThat(service.resolveTenantId(request)).contains(tenantId);
        assertThat(subdomainTenantResolver.isEnabled()).isFalse();
    }

    @Test
    void resolveTenantId_whenNoHeader_returnsEmpty() {
        when(request.getHeaders(SecurityConstants.TENANT_HEADER))
                .thenReturn(Collections.emptyEnumeration());

        assertThat(service.resolveTenantId(request)).isEmpty();
    }
}
