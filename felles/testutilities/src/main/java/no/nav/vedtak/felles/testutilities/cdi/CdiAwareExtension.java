package no.nav.vedtak.felles.testutilities.cdi;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class CdiAwareExtension implements TestInstancePostProcessor {

    static class IgnorantCreationalContext<T> implements CreationalContext<T> {
        @Override
        public void push(T incompleteInstance) {
        }

        @Override
        public void release() {
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {

        var weld = WeldContext.getInstance(); // initialserer cdi container om det ikke har skedd

        var ctx = getRequestContext();
        if (ctx.isActive()) {
            // sørg for at requestcontext alltid er aktiv når denne brukes, også før
            // initialiserer beanss
            ctx.activate();
        }

        var bm = weld.getBeanManager();
        Class cls = testInstance.getClass();
        AnnotatedType annotatedType = bm.createAnnotatedType(cls);
        InjectionTarget injectionTarget = bm.createInjectionTarget(annotatedType);
        injectionTarget.inject(testInstance, new IgnorantCreationalContext<>());
        injectionTarget.postConstruct(testInstance);
    }

    private RequestContext getRequestContext() {
        return CDI.current().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
    }
}
