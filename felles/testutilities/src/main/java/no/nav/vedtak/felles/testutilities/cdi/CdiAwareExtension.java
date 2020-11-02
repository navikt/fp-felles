package no.nav.vedtak.felles.testutilities.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class CdiAwareExtension implements TestInstancePostProcessor {

    static class LazyContextSingleton {
        /** intialiseres ikke før faktisk bruk første gang. */
        private static final BeanManager beanManager = WeldContext.getInstance().getBeanManager();
    }

    static class IgnorantCreationalContext<T> implements CreationalContext<T> {
        @Override
        public void push(T incompleteInstance) {
        }

        @Override
        public void release() {
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        Class cls = testInstance.getClass();
        AnnotatedType annotatedType = LazyContextSingleton.beanManager.createAnnotatedType(cls);
        InjectionTarget injectionTarget = LazyContextSingleton.beanManager.createInjectionTarget(annotatedType);
        injectionTarget.inject(testInstance, new IgnorantCreationalContext<>());
        injectionTarget.postConstruct(testInstance);
    }

}