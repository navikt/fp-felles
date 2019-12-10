package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class ArbeidsfordelingConsumerConfig {
    private static final String ARBEIDSFORDELING_V_1_WSDL = "wsdl/no/nav/tjeneste/virksomhet/arbeidsfordeling/v1/Binding.wsdl";
    private static final String ARBEIDSFORDELING_V_1_NAMESPACE = "http://nav.no/tjeneste/virksomhet/arbeidsfordeling/v1/Binding";
    private static final QName ARBEIDSFORDELING_V_1_SERVICE = new QName(ARBEIDSFORDELING_V_1_NAMESPACE, "Arbeidsfordeling_v1");
    private static final QName ARBEIDSFORDELING_V_1_PORT = new QName(ARBEIDSFORDELING_V_1_NAMESPACE, "Arbeidsfordeling_v1Port");

    private String endpointUrl;

    @Inject
    public ArbeidsfordelingConsumerConfig(@KonfigVerdi("Arbeidsfordeling.v1.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    ArbeidsfordelingV1 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(ARBEIDSFORDELING_V_1_WSDL);
        factoryBean.setServiceName(ARBEIDSFORDELING_V_1_SERVICE);
        factoryBean.setEndpointName(ARBEIDSFORDELING_V_1_PORT);
        factoryBean.setServiceClass(ArbeidsfordelingV1.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(ArbeidsfordelingV1.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
