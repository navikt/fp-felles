package no.nav.vedtak.felles.jpa;

import static io.micrometer.core.instrument.Metrics.globalRegistry;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;

import jakarta.persistence.Persistence;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateHints;
import org.hibernate.orm.micrometer.HibernateMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.jpa.jdbc.DataSourceHolder;


/**
 * Denne klassen initialiserer {@link EntityManagerFactory} ihenhold til angitt
 * konfigurasjon. Benyttes til å sette opp EntityManager gjennom annotasjoner
 * der det er {@link Inject}'ed.
 */
@ApplicationScoped
public class EntityManagerProducer {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerProducer.class);
    private static EntityManagerFactory entityManagerFactory;

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return createNewEntityManager();
    }

    private synchronized EntityManager createNewEntityManager() {
        if (entityManagerFactory == null) {
            entityManagerFactory = getEntityManagerFactory();
        }
        var emf = entityManagerFactory;
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
        // QueryHint (ikke AvailableSettings) - blir ikke automatisk satt på EM.
        em.setFlushMode(FlushModeType.valueOf((String) props.getOrDefault(HibernateHints.HINT_FLUSH_MODE, "COMMIT")));
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.nonJtaDataSource", DataSourceHolder.getDataSource());
            var emf = Persistence.createEntityManagerFactory(NamingStandard.DEFAULT_PERSISTENCE_UNIT, props);
            LOG.info("Muliggjør hibernate monitorering, slås på med hibernate.generate_statistics=true i de enkeltes persistence.xml");
            HibernateMetrics.monitor(globalRegistry, emf.unwrap(SessionFactory.class), NamingStandard.DEFAULT_PERSISTENCE_UNIT);
            entityManagerFactory = emf;
        }
        return entityManagerFactory;
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
