package no.nav.vedtak.felles.integrasjon.behandlejournal;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class BehandleJournalConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/behandleJournal/v3/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/behandleJournal/v3/Binding";
    private static final QName SERVICE = new QName(NAMESPACE, "BehandleJournal_v3");
    private static final QName PORT = new QName(NAMESPACE, "behandleJournal_v3Port");

    private String endpointUrl; // NOSONAR

    @Inject
    public BehandleJournalConsumerConfig(@KonfigVerdi("BehandleJournal_v3.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    BehandleJournalV3 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(BehandleJournalV3.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(BehandleJournalV3.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
