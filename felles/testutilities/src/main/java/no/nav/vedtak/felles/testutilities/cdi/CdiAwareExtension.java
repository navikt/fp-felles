package no.nav.vedtak.felles.testutilities.cdi;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

public class CdiAwareExtension implements TestInstanceFactory, InvocationInterceptor {

    static class LazyContextSingleton {
        /** intialiseres ikke før faktisk bruk første gang. */
        static final WeldContext weldContext = WeldContext.getInstance();
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) throws TestInstantiationException {

        Class<?> testClass = extensionContext.getTestClass()
            .orElseThrow(() -> new TestInstantiationException("test class required"));

        return LazyContextSingleton.weldContext.getBean(testClass);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {

        LazyContextSingleton.weldContext.doWithScope(() -> {
            try {
                return invocation.proceed();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        });
    }

}
