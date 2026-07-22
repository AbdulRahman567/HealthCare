package com.healthcare.hms.tenant.resolution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.healthcare.hms.security.SecurityConstants;
import com.healthcare.hms.tenant.exception.InvalidTenantIdentifierException;
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
class HeaderTenantResolverTest {

    @Mock
    private HttpServletRequest request;

    private HeaderTenantResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new HeaderTenantResolver();
    }

    @Test
    void resolve_whenHeaderPresent_returnsUuid() {
        final UUID tenantId = UUID.randomUUID();
        when(request.getHeaders(SecurityConstants.TENANT_HEADER))
                .thenReturn(Collections.enumeration(List.of(tenantId.toString())));

        assertThat(resolver.resolveTenantId(request)).contains(tenantId);
        assertThat(resolver.source()).isEqualTo(TenantIdentificationSource.REQUEST_HEADER);
    }

    @Test
    void resolve_whenHeaderAbsent_returnsEmpty() {
        when(request.getHeaders(SecurityConstants.TENANT_HEADER))
                .thenReturn(Collections.emptyEnumeration());

        assertThat(resolver.resolveTenantId(request)).isEmpty();
    }

    @Test
    void resolve_whenHeaderBlank_returnsEmpty() {
        when(request.getHeaders(SecurityConstants.TENANT_HEADER))
                .thenReturn(Collections.enumeration(List.of("   ")));

        assertThat(resolver.resolveTenantId(request)).isEmpty();
    }

    @Test
    void resolve_whenHeaderMalformed_throws() {
        when(request.getHeaders(SecurityConstants.TENANT_HEADER))
                .thenReturn(Collections.enumeration(List.of("not-a-uuid")));

        assertThatThrownBy(() -> resolver.resolveTenantId(request))
                .isInstanceOf(InvalidTenantIdentifierException.class)
                .hasMessageContaining("UUID");
    }

    @Test
    void resolve_whenConflictingHeaders_throws() {
        final UUID first = UUID.randomUUID();
        final UUID second = UUID.randomUUID();
        when(request.getHeaders(SecurityConstants.TENANT_HEADER))
                .thenReturn(Collections.enumeration(List.of(first.toString(), second.toString())));

        assertThatThrownBy(() -> resolver.resolveTenantId(request))
                .isInstanceOf(InvalidTenantIdentifierException.class)
                .hasMessageContaining("conflicting");
    }
}
