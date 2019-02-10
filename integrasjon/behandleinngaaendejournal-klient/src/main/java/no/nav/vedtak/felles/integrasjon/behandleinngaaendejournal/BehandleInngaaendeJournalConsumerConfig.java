package no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class BehandleInngaaendeJournalConsumerConfig {

    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/behandleInngaaendeJournal/v1/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/behandleInngaaendeJournal/v1/Binding";

    // TODO (u139158): Gjør verdi påkrevd når får innslag i Fasit
    @Inject
    @KonfigVerdi(value = "BehandleInngaaendeJournal_v1.url", required = false)
    private String endpointUrl; // NOSONAR

    BehandleInngaaendeJournalV1 getPort() {
        return ClientHelper.createServicePort(endpointUrl, BehandleInngaaendeJournalV1.class, WSDL, NAMESPACE,"BehandleInngaaendeJournal_v1", "BehandleInngaaendeJournal_v1Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

}
