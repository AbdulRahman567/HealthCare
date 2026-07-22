package com.healthcare.hms.tenant.persistence;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Binds the Hibernate tenant filter when a JPA transaction starts — after the
 * persistence {@link jakarta.persistence.EntityManager} is associated with the thread.
 *
 * <p>An AOP {@code @Before} on {@code @Transactional} is intentionally not used:
 * the transaction interceptor is the innermost advisor, so a preceding aspect would
 * enable the filter on the wrong session (especially with {@code open-in-view=false}).
 */
@Configuration
public class TenantPersistenceConfig {

    @Bean
    public JpaTransactionManager transactionManager(
            final EntityManagerFactory entityManagerFactory,
            final TenantHibernateFilterEnabler filterEnabler
    ) {
        return new JpaTransactionManager(entityManagerFactory) {
            @Override
            protected void doBegin(final Object transaction, final TransactionDefinition definition) {
                super.doBegin(transaction, definition);
                final Object resource = TransactionSynchronizationManager.getResource(getEntityManagerFactory());
                if (resource instanceof EntityManagerHolder holder) {
                    filterEnabler.enableForCurrentTenant(holder.getEntityManager());
                }
            }
        };
    }
}
