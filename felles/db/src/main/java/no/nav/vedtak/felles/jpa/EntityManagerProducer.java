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
import javax.persistence.Persistence;

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
    @VLPersistenceUnit
    @RequestScoped
    public EntityManager createEntityManager() {
        return createNewEntityManager("pu-default");
    }

    private synchronized EntityManager createNewEntityManager(String key) {

        if (!CACHE_FACTORIES.containsKey(key)) {
            CACHE_FACTORIES.put(key, createEntityManager(key));
        }
        EntityManager em = CACHE_FACTORIES.get(key).createEntityManager();
        return em;
    }

    public EntityManagerFactory createEntityManager(String key) {
        return Persistence.createEntityManagerFactory(key);
    }

    public void dispose(@Disposes @VLPersistenceUnit EntityManager mgr) {
        clearEntityManager(mgr);
    }

    private static void clearEntityManager(EntityManager mgrToDispose) {
        if (mgrToDispose.isOpen()) {
            mgrToDispose.close();
        }
    }

}
