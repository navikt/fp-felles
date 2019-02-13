package no.nav.vedtak.konfig;

import java.util.function.Supplier;

import javax.enterprise.inject.spi.CDI;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.jboss.weld.environment.se.Weld;

class LocalWeldContext {

    private static LocalWeldContext INSTANCE;

    private final Weld weld;

    static synchronized LocalWeldContext getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocalWeldContext();
        }
        return INSTANCE;
    }

    LocalWeldContext() {
        this.weld = new Weld();
        this.weld.initialize();
    }

    <T> T getBean(Class<T> type) {
        return doWithScope(() -> CDI.current().select(type).get());
    }

    private RequestContext getContext() {
        RequestContext requestContext = CDI.current().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
        return requestContext;
    }

    static <V> V doWithScope(Supplier<V> supplier) {
        LocalWeldContext weld = LocalWeldContext.getInstance();

        RequestContext requestContext = weld.getContext();
        if (requestContext.isActive()) {
            return supplier.get();
        } else {
            try {
                requestContext.activate();
                return supplier.get();
            } finally {
                requestContext.invalidate();
                requestContext.deactivate();
            }
        }

    }

}
