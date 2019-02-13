package no.nav.vedtak.felles.integrasjon.infotrygdsak;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.InfotrygdSakV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class InfotrygdSakConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/infotrygdSak/v1/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/infotrygdSak/v1/Binding";
    private static final QName SERVICE = new QName(NAMESPACE, "InfotrygdSak_v1");
    private static final QName PORT = new QName(NAMESPACE, "infotrygdSak_v1Port");

    private String endpointUrl;

    @Inject
    public InfotrygdSakConsumerConfig(@KonfigVerdi("InfotrygdSak_v1.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    InfotrygdSakV1 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(InfotrygdSakV1.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(InfotrygdSakV1.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}