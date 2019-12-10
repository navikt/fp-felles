package no.nav.vedtak.felles.integrasjon.aktør.klient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class AktørConsumerConfig {
    private static final String AKTOER_V_2_WSDL = "wsdl/no/nav/tjeneste/virksomhet/aktoer/v2/Binding.wsdl";
    private static final String AKTOER_V_2_NAMESPACE = "http://nav.no/tjeneste/virksomhet/aktoer/v2/Binding/";
    private static final QName AKTOER_V_2_SERVICE = new QName(AKTOER_V_2_NAMESPACE, "Aktoer");
    private static final QName AKTOER_V_2_PORT = new QName(AKTOER_V_2_NAMESPACE, "Aktoer_v2Port");

    private String endpointUrl;

    @Inject
    public AktørConsumerConfig(@KonfigVerdi("Aktoer.v2.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    AktoerV2 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(AKTOER_V_2_WSDL);
        factoryBean.setServiceName(AKTOER_V_2_SERVICE);
        factoryBean.setEndpointName(AKTOER_V_2_PORT);
        factoryBean.setServiceClass(AktoerV2.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(AktoerV2.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
