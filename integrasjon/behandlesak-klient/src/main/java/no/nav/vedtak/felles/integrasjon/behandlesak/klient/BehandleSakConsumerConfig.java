package no.nav.vedtak.felles.integrasjon.behandlesak.klient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.behandlesak.v2.BehandleSakV2;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class BehandleSakConsumerConfig {
    private static final String BEHANDLE_SAK_V_2_WSDL = "behandlesak/v2/wsdl/BehandleSakV2.wsdl";
    private static final String BEHANDLE_SAK_V2_NAMESPACE = "http://nav.no/tjeneste/virksomhet/behandlesak/v2";

    private String endpointUrl;

    public BehandleSakConsumerConfig() {
        // for CDI proxy
    }

    @Inject
    public BehandleSakConsumerConfig(@KonfigVerdi("BehandleSak_v2.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    BehandleSakV2 getPort() {
        return ClientHelper.createServicePort(endpointUrl, BehandleSakV2.class, BEHANDLE_SAK_V_2_WSDL, BEHANDLE_SAK_V2_NAMESPACE,"BehandleSak_v2", "BehandleSakV2");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
