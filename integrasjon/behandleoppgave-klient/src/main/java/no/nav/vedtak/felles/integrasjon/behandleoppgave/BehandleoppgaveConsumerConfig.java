package no.nav.vedtak.felles.integrasjon.behandleoppgave;


import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class BehandleoppgaveConsumerConfig {
	private static final String WSDL = "behandleoppgave/wsdl/BehandleOppgaveV1.wsdl";
	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/behandleoppgave/v1";
	private static final QName SERVICE = new QName(NAMESPACE, "BehandleOppgave_v1");
	private static final QName PORT = new QName(NAMESPACE, "BehandleOppgaveV1");

    private String endpointUrl;  // NOSONAR

    @Inject
    public BehandleoppgaveConsumerConfig(@KonfigVerdi("Behandleoppgave.v1.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    BehandleOppgaveV1 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(BehandleOppgaveV1.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(BehandleOppgaveV1.class);
    }


    public String getEndpointUrl() {
        return endpointUrl;
    }
}
