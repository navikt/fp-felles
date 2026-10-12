package no.nav.vedtak.felles.testutilities.cdi;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class CdiAwareExtension implements TestInstancePostProcessor {

    @Override
    public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext context) {
        return ExtensionContextScope.TEST_METHOD;
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        var weld = WeldContext.getInstance();
        weld.activateRequestContext(context);
        inject(testInstance, testInstance.getClass(), context);
    }

    private static <T> void inject(Object testInstance, Class<T> type, ExtensionContext context) {
        var manager = WeldContext.getInstance().getBeanManager();
        var annotatedType = manager.createAnnotatedType(type);
        InjectionTarget<T> target = manager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
        T instance = type.cast(testInstance);
        CreationalContext<T> creationalContext = manager.createCreationalContext(null);
        WeldContext.store(context).put(testInstance, new InjectedInstance<>(instance, target, creationalContext));
        target.inject(instance, creationalContext);
        target.postConstruct(instance);
    }

    private record InjectedInstance<T>(T instance, InjectionTarget<T> target, CreationalContext<T> context) implements AutoCloseable {
        @Override
        public void close() {
            try {
                target.preDestroy(instance);
            } finally {
                try {
                    target.dispose(instance);
                } finally {
                    context.release();
                }
            }
        }
    }
}
