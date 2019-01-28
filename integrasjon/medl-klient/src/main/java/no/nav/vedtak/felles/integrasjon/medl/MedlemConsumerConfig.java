package no.nav.vedtak.felles.integrasjon.medl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.medlemskap.v2.MedlemskapV2;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class MedlemConsumerConfig {

    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/medlemskap/v2/MedlemskapV2.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/medlemskap/v2";
    private static final QName SERVICE = new QName(NAMESPACE, "Medlemskap_v2");
    private static final QName PORT = new QName(NAMESPACE, "Medlemskap_v2Port");

    private String endpointUrl;  // NOSONAR

    @Inject
    public MedlemConsumerConfig(@KonfigVerdi("Medlem_v2.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    MedlemskapV2 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(MedlemskapV2.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(MedlemskapV2.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
