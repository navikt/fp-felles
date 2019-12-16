package no.nav.vedtak.felles.jpa;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

/**
 * Interceptor som kobler {@link Transaction} annotasjon til oppsett og commit/rollback av en databasetransaksjon.
 * 
 * @deprecated Bytter til {@link Transactional}. Denne klassen kan fjernes når {@link Transaction} fjernes (erstattes av
 *             {@link TransactionInterceptor}, eller JTA.
 */
@Deprecated(forRemoval = true)
@Transaction
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 10)
@Dependent
public class LegacyTransactionInterceptor {

    private static final class TransactionHandlerInvocation extends TransactionHandler<Object> {
        private final InvocationContext invocationContext;

        private TransactionHandlerInvocation(InvocationContext invocationContext) {
            this.invocationContext = invocationContext;
        }

        @Override
        protected Object doWork(EntityManager em) throws Exception {
            return invocationContext.proceed();
        }
    }

    private static final CDI<Object> CURRENT = CDI.current();

    private void destroyEntityManager(EntityManager entityManager) {
        CURRENT.destroy(entityManager);
    }

    @AroundInvoke
    public Object wrapTransaction(final InvocationContext invocationContext) throws Exception {

        EntityManager entityManager = CURRENT.select(EntityManager.class).get();

        boolean isActiveTx = entityManager.getTransaction().isActive();

        try {
            Object result = new TransactionHandlerInvocation(invocationContext).apply(entityManager);
            return result;
        } finally {
            if (!isActiveTx) {
                destroyEntityManager(entityManager);
            }
        }

    }

}
