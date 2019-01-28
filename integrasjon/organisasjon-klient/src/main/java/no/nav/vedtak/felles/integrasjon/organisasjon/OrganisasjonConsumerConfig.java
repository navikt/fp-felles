package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.security.SecurityConstants;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class OrganisasjonConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/organisasjon/v4/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/organisasjon/v4/Binding";
    private static final QName SERVICE = new QName(NAMESPACE, "Organisasjon_v4");
    private static final QName PORT = new QName(NAMESPACE, "Organisasjon_v4Port");

    private String endpointUrl;  // NOSONAR

    @Inject
    public OrganisasjonConsumerConfig(@KonfigVerdi("Organisasjon_v4.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    OrganisasjonV4 getPort() {
        Map<String, Object> properties = new HashMap<>();
        // FIXME (u139158): Brukes kun ifm mock'en og MÅ fjernes når den har blitt JBoss-ifisert
        properties.put(SecurityConstants.MUST_UNDERSTAND, false);

        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setProperties(properties);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(OrganisasjonV4.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(OrganisasjonV4.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
