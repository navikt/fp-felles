package no.nav.vedtak.felles.integrasjon.person;


import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class PersonConsumerConfig {
    private static final String PERSON_V3_WSDL = "wsdl/no/nav/tjeneste/virksomhet/person/v3/Binding.wsdl";
    private static final String PERSON_V3_NAMESPACE = "http://nav.no/tjeneste/virksomhet/person/v3/Binding";
    private static final QName PERSON_V3_SERVICE = new QName(PERSON_V3_NAMESPACE, "Person_v3");
    private static final QName PERSON_V3_PORT = new QName(PERSON_V3_NAMESPACE, "Person_v3Port");

    private String endpointUrl; // NOSONAR

    @Inject
    public PersonConsumerConfig(@KonfigVerdi("Person_v3.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    PersonV3 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(PERSON_V3_WSDL);
        factoryBean.setServiceName(PERSON_V3_SERVICE);
        factoryBean.setEndpointName(PERSON_V3_PORT);
        factoryBean.setServiceClass(PersonV3.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(PersonV3.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
