package no.nav.vedtak.felles.integrasjon.inntekt;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.inntekt.v3.binding.InntektV3;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class InntektConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/inntekt/v3/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/inntekt/v3/Binding";
    private static final QName SERVICE = new QName(NAMESPACE, "Inntekt_v3");
    private static final QName PORT = new QName(NAMESPACE, "Inntekt_v3Port");

    private String endpointUrl;  // NOSONAR

    @Inject
    public InntektConsumerConfig(@KonfigVerdi("Inntekt_v3.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    InntektV3 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(InntektV3.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(InntektV3.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
