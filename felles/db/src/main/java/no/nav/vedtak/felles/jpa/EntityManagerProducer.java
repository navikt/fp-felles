package no.nav.vedtak.felles.jpa;

import static io.micrometer.core.instrument.Metrics.globalRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateHints;
import org.hibernate.stat.HibernateMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Persistence;


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
    private static final Map<String, EntityManagerFactory> CACHE_FACTORIES = new ConcurrentHashMap<>();

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
        var em = emf.createEntityManager();
        initConfig(em, emf.getProperties());
        return em;
    }

    /**
     * @see org.hibernate.cfg.AvailableSettings
     * @see org.hibernate.jpa.HibernateHints
     */
    private void initConfig(EntityManager em, Map<String, Object> props) {
        // regresson hibernate 4.5.6 - org.hibernate.flushMode er redefinert som
        // QueryHint (ikke AvailableSettings) - blir ikke automatisk satt på
        // EM.
        em.setFlushMode(FlushModeType.valueOf((String) props.getOrDefault(HibernateHints.HINT_FLUSH_MODE, "COMMIT")));
    }

    public EntityManagerFactory createEntityManager(String key) {
        var emf = Persistence.createEntityManagerFactory(key);
        LOG.info("Muliggjør hibernate monitorering, slås på med hibernate.generate_statistics=true i de enkeltes persistence.xml");
        HibernateMetrics.monitor(globalRegistry, emf.unwrap(SessionFactory.class), EM_NAME);
        return emf;
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
