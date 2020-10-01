package no.nav.vedtak.felles.testutilities.db;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.cdi.WeldContext;
import no.nav.vedtak.felles.testutilities.sikkerhet.DummySubjectHandler;
import no.nav.vedtak.felles.testutilities.sikkerhet.SubjectHandlerUtils;

/**
 * Denne erstatter {@link RepositoryRule} i JUnit 5 tester o gir lett tilgang
 * til en EntityManager. Hvis egen initialisering er nødvendig, subklass og
 * override {@link #init()} Brukes slik
 *
 * <pre>
 * &#64;ExtendWith(EntityManagerAwareExtension.class)
 * public class MyjUnit5Test extends EntityManagerAwareTest {
 *
 *     &#64;BeforeEach
 *     public List&#60;String&#62; beforeEach() {
 *        repo = new MyRepo(getEntityManager);
 *     }
 *
 *     &#64;Test
 *     public void testSave() {
 *        repo.save(new MyDTO());
 *        ...
 *     }
 * }
 * </pre>
 */
public class EntityManagerAwareExtension extends PersistenceUnitInitializer implements InvocationInterceptor, TestInstancePostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerAwareExtension.class);
    static {
        SubjectHandlerUtils.useSubjectHandler(DummySubjectHandler.class);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        if (!isTransactional(extensionContext) && shouldCommit(extensionContext)) {
            throw new IllegalStateException("En ikke-transaksjonell test kan ikke commites");
        }
        WeldContext.getInstance().doWithScope(() -> {
            EntityTransaction trans = null;
            try {
                if (isTransactional(extensionContext)) {
                    LOG.trace("Test {} er transaksjonell", target(invocationContext));
                    trans = startTransaction();
                }
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                if (trans != null && trans.isActive()) {
                    if (shouldCommit(extensionContext)) {
                        LOG.info("Commit transaksjonen etter test");
                        trans.commit();
                    } else {
                        LOG.info("{} rollback transaksjonen etter test", target(invocationContext));
                        trans.setRollbackOnly();
                        // trans.rollback();
                    }
                }

                getEntityManager().clear();
            }
            return null;
        });

    }

    private String target(ReflectiveInvocationContext<Method> ctx) {
        return ctx.getTarget().isPresent() ? ctx.getTarget().get().getClass().getSimpleName() : ctx.getTargetClass().getSimpleName();
    }

    private static boolean shouldCommit(ExtensionContext ctx) {
        return ctx.getRequiredTestMethod().getAnnotation(Commit.class) != null;
    }

    private static boolean isTransactional(ExtensionContext ctx) {
        return ctx.getRequiredTestMethod().getAnnotation(NonTransactional.class) == null
                && ctx.getRequiredTestClass().getAnnotation(NonTransactional.class) == null;
    }

    private EntityTransaction startTransaction() {
        EntityTransaction transaction = getEntityManager().getTransaction();
        transaction.begin();
        return transaction;
    }

    @Override
    public EntityManager getEntityManager() {
        return WeldContext.getInstance().doWithScope(super::getEntityManager);
    }

    @Override
    /**
     * Her kan spesifikk initialisering gjøres i en subklasse
     */
    protected void init() {
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        testInstance.getClass()
                .getMethod("setEntityManager", EntityManager.class)
                .invoke(testInstance, getEntityManager());
    }
}
