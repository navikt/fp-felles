package no.nav.vedtak.felles.testutilities.db;

import jakarta.persistence.EntityManager;

/**
 * Tester som bruker {@link EntityManagerAwareExtension} med JUnit 5 kan bruke
 * denne som superklasse, dette gjør at en EntityManger lett kan hentes og
 * brukes i konstruksjon av diverse repositories
 */
public abstract class EntityManagerAwareTest {

    private EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

}
