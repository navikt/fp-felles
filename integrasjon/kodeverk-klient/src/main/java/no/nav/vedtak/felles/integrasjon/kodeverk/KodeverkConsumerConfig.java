package no.nav.vedtak.felles.integrasjon.kodeverk;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.security.SecurityConstants;

import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class KodeverkConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/kodeverk/v2/Kodeverk.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/kodeverk/v2/";
    private static final QName SERVICE = new QName(NAMESPACE, "Kodeverk_v2");
    private static final QName PORT = new QName(NAMESPACE, "Kodeverk_v2");

    private String endpointUrl;  // NOSONAR

    @Inject
    public KodeverkConsumerConfig(@KonfigVerdi("Kodeverk_v2.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    KodeverkPortType getPort() {
        Map<String, Object> properties = new HashMap<>();
        // FIXME (E149421): Tjeneren for kodeverk forstår ikke security-headeren vi sender
        properties.put(SecurityConstants.MUST_UNDERSTAND, false);

        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setProperties(properties);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(KodeverkPortType.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(KodeverkPortType.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
