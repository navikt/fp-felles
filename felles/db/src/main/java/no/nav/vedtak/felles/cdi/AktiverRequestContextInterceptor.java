package no.nav.vedtak.felles.cdi;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.Unbound;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Implementasjon som setter opp {@link RequestContext} når {@link AktiverRequestContext} CDI Scope skal aktiveres.
 * @deprecated Trengs ikke lenger - Bytt til {@link ActivateRequestContext} i stedet (CDI 2.0 har innebygd støtte for dette).
 */
@Deprecated(forRemoval = true,since="2.3.x")
@Interceptor
@AktiverRequestContext
@Priority(Interceptor.Priority.APPLICATION)
@Dependent
public class AktiverRequestContextInterceptor {

    private final BeanManagerImpl beanManager;
    private final RequestContext requestContext;

    @Inject
    public AktiverRequestContextInterceptor(@Unbound RequestContext requestContext, BeanManagerImpl beanManager) {
        this.requestContext = requestContext;
        this.beanManager = beanManager;
    }

    @AroundInvoke
    Object invoke(InvocationContext ctx) throws Exception {
        if (isRequestContextActive()) {
            return ctx.proceed();
        } else {
            try {
                requestContext.activate();
                return ctx.proceed();
            } finally {
                requestContext.invalidate();
                requestContext.deactivate();
            }
        }
    }

    protected boolean isRequestContextActive() {
        return beanManager.isContextActive(RequestScoped.class);
    }
}
