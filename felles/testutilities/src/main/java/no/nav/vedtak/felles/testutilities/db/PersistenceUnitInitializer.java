package no.nav.vedtak.felles.testutilities.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.sql.DataSource;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.felles.jpa.VLPersistenceUnitLiteral;
import no.nav.vedtak.felles.testutilities.UnitTestConfiguration;

/**
 * Denne initialiserer en persistenceunit for bruk i unit testing, basert på default Persistence Unit (angitt
 * {@link VLPersistenceUnit}).
 * <p>
 * Kan subklasses for å integreres med testrammeverk (se {@link RepositoryRule}).
 */
@VLPersistenceUnit
public abstract class PersistenceUnitInitializer {

    private static String persistenceProviderXmlUrl;

    static {
        // last lokale properties
        UnitTestConfiguration.loadUnitTestProperties();
        persistenceProviderXmlUrl = System.getProperty("vl.jpa.persistence.xml", null);
    }

    private static ConcurrentHashMap<String, EntityManagerFactory> initialized = new ConcurrentHashMap<>();
    private EntityManager entityManager;
    private final String key;
    private boolean cdiEnabled = true;

    protected static String findDefaultPersistenceUnit(Class<?> cls) {
        return cls.getAnnotation(VLPersistenceUnit.class).value();
    }

    public PersistenceUnitInitializer() {
        this(findDefaultPersistenceUnit(PersistenceUnitInitializer.class));
    }

    protected PersistenceUnitInitializer(String persistenceUnitKey) {
        init();
        this.key = persistenceUnitKey;
        if (!initialized.containsKey(key)) {
            if (initialized.isEmpty()) {
                initPersistenceProvider();
            }
            initEntityManagerFactory(key);
        }
    }

    /**
     * Nødvendig oppsett som kreves, slik som oppsett av JDNI-oppslag for db
     */
    protected abstract void init();

    private synchronized void initPersistenceProvider() {
        if (!isLocalJetty()) {
            final UnitTestPersistenceUnitProvider provider = new UnitTestPersistenceUnitProvider(persistenceProviderXmlUrl);

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
    }

    protected EntityManagerFactory initEntityManagerFactory(String key) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(key);
        initialized.put(key, emf);
        return emf;
    }

    public EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = createEntityManager();
        }
        return entityManager;
    }

    protected EntityManager createEntityManager() {
        if (!isCdi()) {
            return initialized.get(key).createEntityManager();
        } else {
            return CDI.current().select(EntityManager.class, new VLPersistenceUnitLiteral()).get();
        }
    }

    public static EntityManager createUnmanagedEntityManager(DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.nonJtaDataSource", dataSource);
        properties.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory(findDefaultPersistenceUnit(PersistenceUnitInitializer.class), properties);
        return entityManagerFactory.createEntityManager();
    }

    private static boolean isLocalJetty() {
        return "devimg".equals(System.getProperty("environment.name"));
    }

    protected boolean isCdi() {
        return cdiEnabled;
    }

    protected void setCdi(boolean enabled) {
        this.cdiEnabled = enabled;
    }

}
