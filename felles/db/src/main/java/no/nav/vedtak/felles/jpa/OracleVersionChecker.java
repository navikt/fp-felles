package no.nav.vedtak.felles.jpa;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

/**
 * Sjekker om VL kjører mot Oracle Express Edition, som mangler visse features
 * ift. Enterprise Edition, og dermed krever spesiell SQL i enkelte tilfeller.
 *
 * @deprecated Kan fjernes omgående, kun aktuelt for fpsak/ fpfordel /
 *             fpoppdrag. Bør legges inn der det er behov i stedet.
 */
@Deprecated(forRemoval = true, since = "2.3.x")
@ApplicationScoped
public class OracleVersionChecker {

    private EntityManager entityManager;

    private Boolean cachedRunningOnExpressEdition = null;

    public OracleVersionChecker() {
        // For CDI
    }

    @Inject
    public OracleVersionChecker(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public boolean isRunningOnExpressEdition() {
        if (cachedRunningOnExpressEdition == null) {
            cachedRunningOnExpressEdition = internalIsRunningOnExpressEdition();
        }
        return cachedRunningOnExpressEdition;
    }

    @SuppressWarnings("resource")
    private boolean internalIsRunningOnExpressEdition() {

        // workaround for hibernate issue HHH-11020:
        EntityManager unproxiedEntityManager = entityManager;
        if (unproxiedEntityManager instanceof TargetInstanceProxy) {
            unproxiedEntityManager = (EntityManager) ((TargetInstanceProxy<?>) unproxiedEntityManager).weld_getTargetInstance();
        }

        final Boolean[] resHolder = { false };

        Session session = unproxiedEntityManager.unwrap(Session.class);
        session.doWork(new Work() { // NOSONAR vi må kunne nå resHolder
            @Override
            public void execute(Connection connection) throws SQLException {
                DatabaseMetaData metaData = connection.getMetaData();
                String prodName = metaData.getDatabaseProductName();
                String prodVer = metaData.getDatabaseProductVersion();
                if (prodName.contains("Oracle") && prodVer.contains("Express")) {
                    resHolder[0] = true;
                }
            }
        });

        return resHolder[0];
    }
}
