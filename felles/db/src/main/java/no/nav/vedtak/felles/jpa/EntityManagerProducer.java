package no.nav.vedtak.felles.jpa;

import static io.micrometer.core.instrument.Metrics.globalRegistry;

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

import org.hibernate.SessionFactory;
import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.binder.jpa.HibernateMetrics;

/**
 * Denne klassen initialiserer {@link EntityManagerFactory} ihenhold til angitt
 * konfigurasjon. Benyttes til å sette opp EntityManager gjennom annotasjoner
 * der det er {@link Inject}'ed.
 */
@ApplicationScoped
public class EntityManagerProducer {

    private static final String EM_NAME = "pu-default";
    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerProducer.class);
    /**
     * registrerte {@link EntityManagerFactory}.
     */
    private static final Map<String, EntityManagerFactory> CACHE_FACTORIES = new ConcurrentHashMap<>(); // NOSONAR

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return createNewEntityManager(EM_NAME);
    }

    private synchronized EntityManager createNewEntityManager(String key) {
        if (!CACHE_FACTORIES.containsKey(key)) {
            CACHE_FACTORIES.put(key, createEntityManager(key));
        }
        var emf = CACHE_FACTORIES.get(key);
        var sf = emf.unwrap(SessionFactory.class);
        LOG.info("Muliggjør hibernate monitorering, slås på med hibernate.generate_statistics=true i de enkeltes persistence.xml");
        HibernateMetrics.monitor(globalRegistry, sf, EM_NAME);
        // HibernateQueryMetrics.monitor(globalRegistry, sf, EM_NAME);
        var em = emf.createEntityManager();
        initConfig(em, emf.getProperties());
        return em;
    }

    /**
     * @see org.hibernate.cfg.AvailableSettings
     * @see org.hibernate.jpa.QueryHints
     */
    private void initConfig(EntityManager em, Map<String, Object> props) {
        // regresson hibernate 4.5.6 - org.hibernate.flushMode er redefinert som
        // QueryHint (ikke AvailableSettings) - blir ikke automatisk satt på
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
