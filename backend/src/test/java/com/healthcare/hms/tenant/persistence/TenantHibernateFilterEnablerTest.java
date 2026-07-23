package com.healthcare.hms.tenant.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.common.exception.authorization.RoleDeniedException;
import com.healthcare.hms.common.persistence.TenantPersistence;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.users.enums.RoleType;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class TenantHibernateFilterEnablerTest {

    private final TenantHibernateFilterEnabler enabler = new TenantHibernateFilterEnabler();

    @AfterEach
    void clearContext() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Enables Hibernate tenantFilter with the bound tenant id")
    void enableForCurrentTenant_whenContextPresent_enablesFilter() {
        final UUID tenantId = UUID.randomUUID();
        TenantContextHolder.set(new TenantContext(
                tenantId,
                "slug",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        final EntityManager entityManager = mock(EntityManager.class);
        final Session session = mock(Session.class);
        final Filter filter = mock(Filter.class);
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.enableFilter(TenantPersistence.FILTER_NAME)).thenReturn(filter);

        enabler.enableForCurrentTenant(entityManager);

        verify(session).enableFilter(TenantPersistence.FILTER_NAME);
        verify(filter).setParameter(TenantPersistence.PARAM_TENANT_ID, tenantId);
    }

    @Test
    @DisplayName("Does not enable the filter when no tenant context is bound")
    void enableForCurrentTenant_whenNoContext_leavesFilterDisabled() {
        final EntityManager entityManager = mock(EntityManager.class);
        final Session session = mock(Session.class);
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.getEnabledFilter(TenantPersistence.FILTER_NAME)).thenReturn(null);

        enabler.enableForCurrentTenant(entityManager);

        verify(session, never()).enableFilter(TenantPersistence.FILTER_NAME);
    }

    @Test
    @DisplayName("executeWithoutTenantFilter restores previous filter state for platform Super Admin")
    void executeWithoutTenantFilter_restoresFilter() {
        bindPlatformSuperAdmin();
        final UUID tenantId = UUID.randomUUID();
        TenantContextHolder.set(new TenantContext(
                tenantId,
                "slug",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        final EntityManager entityManager = mock(EntityManager.class);
        final Session session = mock(Session.class);
        final Filter filter = mock(Filter.class);
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.getEnabledFilter(TenantPersistence.FILTER_NAME)).thenReturn(filter);
        when(session.enableFilter(TenantPersistence.FILTER_NAME)).thenReturn(filter);

        final String result = enabler.executeWithoutTenantFilter(
                entityManager,
                "unit-test-restore",
                () -> "ok"
        );

        assertThat(result).isEqualTo("ok");
        verify(session).disableFilter(TenantPersistence.FILTER_NAME);
        verify(session).enableFilter(TenantPersistence.FILTER_NAME);
        verify(filter).setParameter(TenantPersistence.PARAM_TENANT_ID, tenantId);
    }

    @Test
    @DisplayName("executeWithoutTenantFilter rejects blank reasons")
    void executeWithoutTenantFilter_rejectsBlankReason() {
        final EntityManager entityManager = mock(EntityManager.class);

        assertThatThrownBy(() ->
                enabler.executeWithoutTenantFilter(entityManager, "  ", () -> "x")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("executeWithoutTenantFilter rejects unauthenticated callers")
    void executeWithoutTenantFilter_rejectsUnauthenticated() {
        final EntityManager entityManager = mock(EntityManager.class);

        assertThatThrownBy(() ->
                enabler.executeWithoutTenantFilter(entityManager, "bootstrap", () -> "x")
        ).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("executeWithoutTenantFilter rejects non-platform hospital admins")
    void executeWithoutTenantFilter_rejectsHospitalAdmin() {
        final AuthenticatedUser hospitalAdmin = new AuthenticatedUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "admin@hospital.test",
                Set.of(RoleType.Names.HOSPITAL_ADMIN),
                Set.of(),
                1
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(hospitalAdmin, null, hospitalAdmin.getAuthorities())
        );

        final EntityManager entityManager = mock(EntityManager.class);

        assertThatThrownBy(() ->
                enabler.executeWithoutTenantFilter(entityManager, "cross-tenant", () -> "x")
        ).isInstanceOf(RoleDeniedException.class);
    }

    private static void bindPlatformSuperAdmin() {
        final AuthenticatedUser superAdmin = new AuthenticatedUser(
                UUID.randomUUID(),
                null,
                "super@platform.test",
                Set.of(RoleType.Names.SUPER_ADMIN),
                Set.of(),
                1
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(superAdmin, null, superAdmin.getAuthorities())
        );
    }
}
