package no.nav.vedtak.felles.testutilities.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Instantierer Weld (CDI container) for enhetstesting.
 */
public class WeldContext implements AutoCloseable {

    private static WeldContext INSTANCE;
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(WeldContext.class);

    private final WeldContainer container;
    private final List<Runnable> beanDestructors = new ArrayList<>();

    public static synchronized WeldContext getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WeldContext();
        }
        return INSTANCE;
    }

    public WeldContext() {
        // syntetisk bean
        container = new Weld().property("org.jboss.weld.se.archive.isolation", false).initialize();
    }

    public BeanManager getBeanManager() {
        return container.getBeanManager();
    }

    public <T> T getBean(Class<T> type) {
        return doWithScope(() -> {
            BeanManager manager = container.getBeanManager();
            AnnotatedType<T> oat = manager.createAnnotatedType(type);
            BeanAttributes<T> oa = manager.createBeanAttributes(oat);
            InjectionTargetFactory<T> factory = manager.getInjectionTargetFactory(oat);
            Bean<T> bean = manager.createBean(oa, type, factory);
            CreationalContext<T> context = manager.createCreationalContext(bean);
            T instance = bean.create(context);
            synchronized (beanDestructors) {
                beanDestructors.add(() -> bean.destroy(instance, context));
            }
            return instance;
        });
    }

    public void activateRequestContext(ExtensionContext context) {
        context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(WeldContext.class, _ -> this, WeldContext.class);
        store(context).getOrComputeIfAbsent(RequestScope.class, _ -> new RequestScope(), RequestScope.class);
    }

    static ExtensionContext.Store store(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    public <V> V doWithScope(Supplier<V> supplier) {
        try (var scope = new RequestScope()) {
            return supplier.get();
        }
    }

    @Override
    public void close() {
        try {
            synchronized (beanDestructors) {
                beanDestructors.reversed().forEach(Runnable::run);
                beanDestructors.clear();
            }
        } finally {
            try {
                container.close();
            } finally {
                synchronized (WeldContext.class) {
                    if (INSTANCE == this) {
                        INSTANCE = null;
                    }
                }
            }
        }
    }

    private static final class RequestScope implements AutoCloseable {
        private final Instance.Handle<RequestContextController> handle = CDI.current().select(RequestContextController.class).getHandle();
        private final boolean activated = handle.get().activate();

        @Override
        public void close() {
            try {
                if (activated) {
                    handle.get().deactivate();
                }
            } finally {
                handle.close();
            }
        }
    }
}
