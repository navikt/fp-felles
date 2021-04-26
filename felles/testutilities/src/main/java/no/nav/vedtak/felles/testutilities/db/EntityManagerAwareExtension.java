package no.nav.vedtak.felles.testutilities.db;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.cdi.WeldContext;

/**
 * Denne erstatter {@link RepositoryRule} i JUnit 5 tester o gir lett tilgang
 * til en EntityManager. Hvis egen initialisering er nødvendig, subklass og
 * override {@link #init()} Brukes slik
 *
 * <pre>
 * &#64;ExtendWith(EntityManagerAwareExtension.class)
 * public class MyjUnit5Test  {
 *
 *     &#64;BeforeEach
 *     public List&#60;String&#62; beforeEach(EntityManager em) {
 *        repo = new MyRepo(em);
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
public class EntityManagerAwareExtension extends PersistenceUnitInitializer
        implements ParameterResolver, InvocationInterceptor, TestInstancePostProcessor, TestInstancePreDestroyCallback {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerAwareExtension.class);

    static {
        WeldContext.getInstance(); // init cdi container
    }

    /**
     * Her kan spesifikk initialisering gjøres i en subklasse
     */
    @Override
    protected void init() {
        // NB: denne
    }

    @Override
    public EntityManager getEntityManager() {
        /**
         * trenger hente her med scope i tilfelle denne kalles i initialisering av felter (før *
         * {@link #postProcessTestInstance(Object, ExtensionContext)} er kalt). Returnerer en proxy som kan aktiveres senere.
         */
        return WeldContext.getInstance().doWithScope(this::internalEntityManager);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {

        var ctx = getRequestContext();
        if (!ctx.isActive()) {
            // sørg for at RequestContext alltid er aktiv før initialserer mer
            ctx.activate();
        }

        // kaller på hvis finnes
        Optional<Method> methodToFind = Arrays.stream(testInstance.getClass().getMethods())
            .filter(method -> "setEntityManager".equals(method.getName()))
            .findFirst();
        if (methodToFind.isPresent()) {
            methodToFind.get().invoke(testInstance, internalEntityManager());
        }

    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

        // start tx her i tilfelle BeforeEach method er definert. Avventer rollback til preDestroy..
        startTransaction(extensionContext);
        invocation.proceed();

    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext)
            throws Throwable {
        // start tx her i tilfelle BeforeEach method ikke var definert. Avventer rollback til preDestroy..
        startTransaction(extensionContext);
        invocation.proceed();
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext extensionContext) throws Exception {
        var ctx = getRequestContext();
        var em = internalEntityManager();
        try {
            stopTransaction(extensionContext, em.getTransaction());
        } finally {
            if (ctx.isActive()) {
                ctx.deactivate();
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(EntityManager.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return internalEntityManager();
    }

    private RequestContext getRequestContext() {
        return CDI.current().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
    }

    private EntityManager internalEntityManager() {
        // henter uten scope (intern optimalisering)
        return super.getEntityManager();
    }

    private void stopTransaction(ExtensionContext extensionContext, EntityTransaction trans) {
        if (trans != null && trans.isActive()) {
            if (shouldCommit(extensionContext)) {
                LOG.debug("Commit transaksjonen etter test: {}", extensionContext.getRequiredTestMethod());
                trans.commit();
            } else {
                LOG.debug("{} rollback transaksjonen etter test", extensionContext.getRequiredTestMethod());
                trans.rollback();
            }
        }
    }

    private static boolean shouldCommit(ExtensionContext ctx) {
        return ctx.getRequiredTestMethod().getAnnotation(Commit.class) != null;
    }

    private static boolean isTransactional(ExtensionContext ctx) {
        return ctx.getRequiredTestClass().getAnnotation(NonTransactional.class) == null
            && ctx.getRequiredTestMethod().getAnnotation(NonTransactional.class) == null;
    }

    private void startTransaction(ExtensionContext extensionContext) {
        if (isTransactional(extensionContext)) {
            var transaction = internalEntityManager().getTransaction();
            if (!transaction.isActive()) {
                transaction.begin();
            } else {
                // er allerede aktiv tx, do nothing
            }
        } else if (shouldCommit(extensionContext)) {
            throw new IllegalStateException("En ikke-transaksjonell test kan ikke commites: " + extensionContext.getRequiredTestMethod());
        }
    }
}
