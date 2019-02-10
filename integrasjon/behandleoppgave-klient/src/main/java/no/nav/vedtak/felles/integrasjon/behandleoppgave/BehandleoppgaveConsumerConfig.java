package no.nav.vedtak.felles.integrasjon.behandleoppgave;


import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class BehandleoppgaveConsumerConfig {
	private static final String WSDL = "behandleoppgave/wsdl/BehandleOppgaveV1.wsdl";
	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/behandleoppgave/v1";

    private String endpointUrl;  // NOSONAR

    @Inject
    public BehandleoppgaveConsumerConfig(@KonfigVerdi("Behandleoppgave_v1.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    BehandleOppgaveV1 getPort() {
        return ClientHelper.createServicePort(endpointUrl, BehandleOppgaveV1.class, WSDL, NAMESPACE,"BehandleOppgave_v1", "BehandleOppgaveV1");
    }


    public String getEndpointUrl() {
        return endpointUrl;
    }
}
