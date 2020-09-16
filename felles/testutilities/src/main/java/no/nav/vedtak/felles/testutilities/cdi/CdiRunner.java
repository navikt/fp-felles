package no.nav.vedtak.felles.testutilities.cdi;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Oppsett av Dependency Injection (CDI) for enhetstester.
 * <p>
 * Gjør at CDI enabled beans kan injectes direkte i enhetstestklasser.
 */
@Deprecated(forRemoval = true, since = "2.3.x")
public class CdiRunner extends BlockJUnit4ClassRunner {

    private WeldContext weldContext;

    public CdiRunner(Class<Object> clazz) throws InitializationError {
        super(clazz);

        weldContext = WeldContext.getInstance();
    }

    @Override
    protected Object createTest() {
        final Class<?> test = getTestClass().getJavaClass();

        return weldContext.getBean(test);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        weldContext.doWithScope(() -> {
            super.runChild(method, notifier);
            return null;
        });
    }

}
