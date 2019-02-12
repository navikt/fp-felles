package no.nav.vedtak.felles.testutilities.db;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

public class Repository {

    private EntityManager entityManager;

    public Repository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> T hent(Class<T> klasse, long id) {
        return entityManager.find(klasse, id);
    }

    public <T> T hent(Class<T> klasse, String id) {
        return entityManager.find(klasse, id);
    }

    public <T> List<T> hentAlle(Class<T> klasse) {
        CriteriaQuery<T> criteria = entityManager.getCriteriaBuilder().createQuery(klasse);
        criteria.select(criteria.from(klasse));
        return entityManager.createQuery(criteria).getResultList();
    }

    public <T> void lagre(T entitet) {
        entityManager.persist(entitet);
    }

    public <T> void lagre(Collection<T> entiteter) {
        entiteter.forEach(e -> entityManager.persist(e));
    }

    public void flush() {
        entityManager.flush();
    }

    public void clear() {
        entityManager.clear();
    }

    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
