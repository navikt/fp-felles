package no.nav.vedtak.konfig;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class LocalCdiRunner extends BlockJUnit4ClassRunner {

    public LocalCdiRunner(Class<Object> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected Object createTest() {
        final Class<?> test = getTestClass().getJavaClass();

        LocalWeldContext weldContext = LocalWeldContext.getInstance();
        return weldContext.getBean(test);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        
        LocalWeldContext.doWithScope(() -> {
            super.runChild(method, notifier);
            return null;
        });
    }

}
