package no.nav.vedtak.sikkerhet.jaspic.soap;

import static javax.security.auth.message.AuthStatus.FAILURE;
import static javax.security.auth.message.AuthStatus.SUCCESS;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.servlet.http.HttpServletRequest;

import org.apache.wss4j.dom.handler.WSHandlerConstants;

import no.nav.vedtak.sikkerhet.jaspic.DelegatedProtectedResource;

/**
 * Delegert protection fra OIDC Auth Module for håndtering av Soap på egen
 * servlet.
 */
public class SoapProtectedResource implements DelegatedProtectedResource {

    private static class LazyInit {
        static final WSS4JProtectedServlet wsServlet;
        static final Set<String> wsServletPaths;
        static {
            wsServlet = findWSS4JProtectedServlet();
            wsServletPaths = findWsServletPaths(wsServlet);
        }

        private static Set<String> findWsServletPaths(WSS4JProtectedServlet wsServlet) {
            return wsServlet == null ? Collections.emptySet() : Set.copyOf(wsServlet.getUrlPatterns());
        }

        private static WSS4JProtectedServlet findWSS4JProtectedServlet() {
            // No need for bean.destroy(instance) since it's ApplicationScoped
            var instance = CDI.current().select(WSS4JProtectedServlet.class);
            if (instance.isResolvable()) {
                return instance.get();
            } else {
                // hvis applikasjonen ikke tilbyr webservice, har den heller ikke
                // WSS4JProtectedServlet
                return null;
            }
        }

    }

    public SoapProtectedResource() {
    }

    @Override
    public Optional<AuthStatus> handleProtectedResource(HttpServletRequest originalRequest, Subject clientSubject,
            CallbackHandler containerCallbackHandler) {
        if (usingSamlForAuthentication(originalRequest)) {
            if (LazyInit.wsServlet.isProtectedWithAction(originalRequest.getPathInfo(), WSHandlerConstants.SAML_TOKEN_SIGNED)) {
                try {
                    containerCallbackHandler.handle(new Callback[] { new CallerPrincipalCallback(clientSubject, "SAML") });
                } catch (IOException | UnsupportedCallbackException e) {
                    // Should not happen
                    throw new IllegalStateException(e);
                }
                return Optional.of(SUCCESS);
            } else {
                return Optional.of(FAILURE);
            }
        }
        return Optional.empty(); // ikke håndtert av denne
    }

    private static boolean usingSamlForAuthentication(HttpServletRequest request) {
        return !isGET(request) && request.getServletPath() != null && LazyInit.wsServletPaths.contains(request.getServletPath());
    }

    /**
     * JAX-WS only supports SOAP over POST
     *
     * Hentet fra WSS4JInInterceptor#isGET(SoapMessage)
     */
    private static final boolean isGET(HttpServletRequest request) {
        return "GET".equals(request.getMethod());
    }

}
