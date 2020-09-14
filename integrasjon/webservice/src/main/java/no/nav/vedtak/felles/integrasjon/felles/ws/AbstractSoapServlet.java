package no.nav.vedtak.felles.integrasjon.felles.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;

import no.nav.vedtak.sikkerhet.jaspic.soap.WSS4JProtectedServlet;

public abstract class AbstractSoapServlet extends CXFNonSpringServlet implements WSS4JProtectedServlet {

    private final VLFaultListener faultListener = new VLFaultListener();
    private final Map<String, EndpointImpl> map = new HashMap<>();

    @SuppressWarnings("resource")
    protected void publish(Object implementor) {
        ensureWeGotSoapBus();
        // Bør ikke lukkes fordi da server vi ikke endepunktet lenger.
        EndpointImpl endpoint = new EndpointImpl(getBus(), implementor); // NOSONAR
        String address = getEndpointFromAnnotation(implementor.getClass());
        endpoint.getInInterceptors().add(new SAMLTokenSignedInInterceptor());
        endpoint.getInInterceptors().add(new CallIdInInterceptor());
        endpoint.publish(address);
        map.put(address, endpoint);
    }

    private void ensureWeGotSoapBus() {
        if (getBus() == null) {
            loadBus(null);
            getBus().getProperties().put(FaultListener.class.getName(), faultListener); // FIXME: FaultListener gir Nullpointer ved ukjent mustUnderstand header f.eks
            getBus().getFeatures().add(new WSAddressingFeature());
        }
    }

    @SuppressWarnings("resource")
    @Override
    public boolean isProtectedWithAction(String pathInfo, String requiredAction) {
        if (map.containsKey(pathInfo)) {
            EndpointImpl endpoint = map.get(pathInfo);
            if (!endpoint.isPublished()) {
                map.remove(pathInfo, endpoint);
                return false;
            }
            if (hasInterceptorWithRequiredAction(endpoint.getInInterceptors(), requiredAction)) {
                return true;
            }
            if (hasInterceptorWithRequiredAction(endpoint.getBus().getInInterceptors(), requiredAction)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInterceptorWithRequiredAction(List<Interceptor<? extends Message>> interceptors, String requiredAction) {
        for (Interceptor<? extends Message> interceptor : interceptors) {
            if (interceptor instanceof WSS4JInInterceptor) {
                String action = (String) ((WSS4JInInterceptor) interceptor).getOption(ConfigurationConstants.ACTION);
                if (action != null && action.contains(requiredAction)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Inject
    public void setUnntakKonfigurasjon(@Any Instance<VLFaultListenerUnntakKonfigurasjon> unntakKonfigurasjon) {
        for (VLFaultListenerUnntakKonfigurasjon konfigurasjon : unntakKonfigurasjon) {
            faultListener.leggTilUnntak(konfigurasjon);
        }
    }

    private String getEndpointFromAnnotation(Class<?> clazz) {
        if (clazz.isAnnotationPresent(SoapWebService.class)) {
            return clazz.getAnnotation(SoapWebService.class).endpoint();
        }

        throw new IllegalStateException("Endpoint er ikke deklarert for WebServicen");
    }
}
