package com.healthcare.hms.tenant.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.common.persistence.TenantPersistence;
import com.healthcare.hms.support.AbstractMySqlIntegrationTest;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.entity.Tenant;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.tenant.repository.TenantRepository;
import com.healthcare.hms.users.entity.User;
import com.healthcare.hms.users.enums.UserStatus;
import com.healthcare.hms.users.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifies Hibernate tenant filtering: a repository cannot return another tenant's rows
 * while {@link TenantContextHolder} is bound.
 */
@Import(TenantAwarePersistenceIntegrationTest.TenantScopedUserLookup.class)
class TenantAwarePersistenceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantScopedUserLookup tenantScopedUserLookup;

    @AfterEach
    void clearContext() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("findById returns empty for a user belonging to another tenant")
    void findById_withTenantContext_doesNotLeakForeignTenant() {
        final Tenant tenantA = persistActiveTenant("alpha");
        final Tenant tenantB = persistActiveTenant("beta");
        final User userA = persistUser(tenantA.getId(), "user-a@" + tenantA.getSlug() + ".test");
        final User userB = persistUser(tenantB.getId(), "user-b@" + tenantB.getSlug() + ".test");

        bind(tenantA.getId());

        final Optional<User> own = tenantScopedUserLookup.findById(userA.getId());
        final Optional<User> foreign = tenantScopedUserLookup.findById(userB.getId());

        assertThat(own).isPresent();
        assertThat(own.get().getId()).isEqualTo(userA.getId());
        assertThat(foreign).isEmpty();
    }

    @Test
    @DisplayName("Without tenant context, findById can resolve any tenant (auth/bootstrap paths)")
    void findById_withoutTenantContext_allowsGlobalLookup() {
        final Tenant tenant = persistActiveTenant("gamma");
        final User user = persistUser(tenant.getId(), "user@" + tenant.getSlug() + ".test");

        assertThat(TenantContextHolder.isPresent()).isFalse();
        assertThat(tenantScopedUserLookup.findById(user.getId())).isPresent();
    }

    @Test
    @DisplayName("PrePersist auto-stamps tenant_id from context on save")
    void save_withTenantContext_autoStampsTenantId() {
        final Tenant tenant = persistActiveTenant("delta");
        bind(tenant.getId());

        final User user = new User();
        user.setFirstName("Auto");
        user.setLastName("Stamp");
        user.setEmail("auto-stamp@" + tenant.getSlug() + ".test");
        user.setPasswordHash(passwordEncoder.encode("StrongPass1!ab"));
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);

        final User saved = tenantScopedUserLookup.save(user);

        assertThat(saved.getTenantId()).isEqualTo(tenant.getId());
    }

    private Tenant persistActiveTenant(final String key) {
        final String suffix = UUID.randomUUID().toString().substring(0, 8);
        final Tenant tenant = new Tenant();
        tenant.setName("Hospital " + key + " " + suffix);
        tenant.setSlug(key + "-" + suffix);
        tenant.setTenantType(TenantType.HOSPITAL);
        tenant.setEmail(key + "-" + suffix + "@hospital.test");
        tenant.setSubscriptionPlan(SubscriptionPlan.BASIC);
        tenant.setStatus(TenantStatus.ACTIVE);
        return tenantRepository.saveAndFlush(tenant);
    }

    private User persistUser(final UUID tenantId, final String email) {
        final User user = new User();
        user.setTenantId(tenantId);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("StrongPass1!ab"));
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        return userRepository.saveAndFlush(user);
    }

    private static void bind(final UUID tenantId) {
        TenantContextHolder.set(new TenantContext(
                tenantId,
                "bound-tenant",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));
    }

    /**
     * Transactional facade so {@link TenantPersistenceConfig} enables the Hibernate
     * tenant filter on transaction begin before repository access.
     */
    @Service
    static class TenantScopedUserLookup {

        private final UserRepository userRepository;

        TenantScopedUserLookup(final UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Transactional(readOnly = true)
        public Optional<User> findById(final UUID id) {
            return userRepository.findById(id);
        }

        @Transactional
        public User save(final User user) {
            return userRepository.saveAndFlush(user);
        }
    }
}
