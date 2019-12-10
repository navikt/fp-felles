package no.nav.vedtak.felles.integrasjon.behandlesak.klient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.behandlesak.v2.BehandleSakV2;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class BehandleSakConsumerConfig {
    private static final String BEHANDLE_SAK_V_2_WSDL = "behandlesak/v2/wsdl/BehandleSakV2.wsdl";
    private static final String BEHANDLE_SAK_V2_NAMESPACE = "http://nav.no/tjeneste/virksomhet/behandlesak/v2";
    private static final QName BEHANDLE_SAK_V_2_SERVICE = new QName(BEHANDLE_SAK_V2_NAMESPACE, "BehandleSak_v2");
    private static final QName BEHANDLE_SAK_V_2_PORT = new QName(BEHANDLE_SAK_V2_NAMESPACE, "BehandleSakV2");

    private String endpointUrl;

    public BehandleSakConsumerConfig() {
        // for CDI proxy
    }

    @Inject
    public BehandleSakConsumerConfig(@KonfigVerdi("BehandleSak.v2.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    BehandleSakV2 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(BEHANDLE_SAK_V_2_WSDL);
        factoryBean.setServiceName(BEHANDLE_SAK_V_2_SERVICE);
        factoryBean.setEndpointName(BEHANDLE_SAK_V_2_PORT);
        factoryBean.setServiceClass(BehandleSakV2.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(BehandleSakV2.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
