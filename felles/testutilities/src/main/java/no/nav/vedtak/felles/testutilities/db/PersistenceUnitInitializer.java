package no.nav.vedtak.felles.testutilities.db;

import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import no.nav.vedtak.felles.testutilities.UnitTestConfiguration;

/**
 * Denne initialiserer en persistenceunit for bruk i unit testing, basert på default Persistence Unit
 * . Integerer også automatisk CDI oppsett, med mindre det disables
 * <p>
 * Kan subklasses for å integreres med testrammeverk (se {@link RepositoryRule}).
 */
public abstract class PersistenceUnitInitializer {

    static {
        // last lokale properties
        UnitTestConfiguration.loadUnitTestProperties();
        initPersistenceProvider();
    }

    private EntityManager entityManager;

    public PersistenceUnitInitializer() {
        init();
    }

    /**
     * Nødvendig oppsett som kreves, slik som oppsett av JDNI-oppslag for db
     */
    protected abstract void init();

    private static void initPersistenceProvider() {
        final UnitTestPersistenceUnitProvider provider = new UnitTestPersistenceUnitProvider();

        PersistenceProviderResolverHolder.setPersistenceProviderResolver(new PersistenceProviderResolver() {

            @Override
            public List<PersistenceProvider> getPersistenceProviders() {
                return Collections.singletonList(provider);
            }

            @Override
            public void clearCachedProviders() {
            }
        });
    }

    protected EntityManagerFactory initEntityManagerFactory(String key) {
        EntityManagerFactory emf = createEntityManagerFactory(key);
        return emf;
    }

    protected EntityManagerFactory createEntityManagerFactory(String key) {
        return Persistence.createEntityManagerFactory(key);
    }

    public EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = createEntityManager();
        }
        return entityManager;
    }

    protected EntityManager createEntityManager() {
        return CDI.current().select(EntityManager.class).get();
    }

}
