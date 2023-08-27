package no.nav.vedtak.felles.jpa.savepoint;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import jakarta.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

/**
 * Kjører et stykke jobb med et savepoint for rollback mot database. Gir samme
 * effekt som 'nested transactions' og abstraherer bort jpa provider (Hibernate
 * Work ifc).
 */
public class RunWithSavepoint {
    private final EntityManager em;

    @SuppressWarnings("rawtypes")
    public RunWithSavepoint(EntityManager em) {
        Objects.requireNonNull(em, "em");
        // workaround for hibernate issue HHH-11020
        if (em instanceof TargetInstanceProxy) {
            em = (EntityManager) ((TargetInstanceProxy) em).weld_getTargetInstance();
        }
        this.em = em;

    }

    public <V> V doWork(Work<V> work) {
        // sørg for at alle endringer er synket til db
        em.flush();

        try {
            var session = em.unwrap(Session.class); // ikke close her (håndteres når tx lukkes)
            if (session.getTransaction().getRollbackOnly()) {
                throw new IllegalStateException("Kan ikke opprette savepoint for connection som er markert for rollback-only");
            }
            return session.doReturningWork(new ReturningWork<V>() {

                @Override
                public V execute(Connection conn) throws SQLException {
                    if (conn.isReadOnly()) {
                        // skal vel aldri skje, men
                        return work.doWork();
                    } else {
                        var savepoint = conn.setSavepoint();
                        try {
                            V result = work.doWork();
                            return result;
                        } catch (SavepointRolledbackException e) {
                            // allerede skjedd, ikke håndter på nytt men la 'vårt' savepoint i fred
                            throw e;
                        } catch (Throwable t) { // NOSONAR
                            // alle andre feil intercepts medfører rollback siden vi ikke kan være sikre på tilstand.
                            em.clear(); // rydd ugyldig state
                            if (!conn.isClosed()) {
                                conn.rollback(savepoint);
                            }
                            throw new SavepointRolledbackException(t);
                        }
                    }
                }
            });
        } finally {
            em.flush();
        }
    }
}
