package no.nav.vedtak.felles.jpa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

import org.hibernate.jpa.QueryHints;

/**
 * Denne klassen initialiserer {@link EntityManagerFactory} ihenhold til angitt konfigurasjon.
 * Benyttes til å sette opp EntityManager gjennom annotasjoner der det er {@link Inject}'ed.
 */
@ApplicationScoped
public class EntityManagerProducer {

    /**
     * registrerte {@link EntityManagerFactory}.
     */
    private static final Map<String, EntityManagerFactory> CACHE_FACTORIES = new ConcurrentHashMap<>(); // NOSONAR

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return createNewEntityManager("pu-default");
    }

    private synchronized EntityManager createNewEntityManager(String key) {

        if (!CACHE_FACTORIES.containsKey(key)) {
            CACHE_FACTORIES.put(key, createEntityManager(key));
        }
        EntityManagerFactory entityManagerFactory = CACHE_FACTORIES.get(key);
        EntityManager em = entityManagerFactory.createEntityManager();
        initConfig(em, entityManagerFactory.getProperties());
        return em;
    }

    /**
     * @see org.hibernate.cfg.AvailableSettings
     * @see org.hibernate.jpa.QueryHints
     */
    private void initConfig(EntityManager em, Map<String, Object> props) {
        // regresson hibernate 4.5.6 - org.hibernate.flushMode er redefinert som QueryHint (ikke AvailableSettings) - blir ikke automatisk satt på
        // EM.
        em.setFlushMode(FlushModeType.valueOf((String) props.getOrDefault(QueryHints.HINT_FLUSH_MODE, "COMMIT")));
    }

    public EntityManagerFactory createEntityManager(String key) {
        return Persistence.createEntityManagerFactory(key);
    }

    public void dispose(@Disposes EntityManager mgr) {
        clearEntityManager(mgr);
    }

    private static void clearEntityManager(EntityManager mgrToDispose) {
        if (mgrToDispose.isOpen()) {
            mgrToDispose.close();
        }
    }

}
