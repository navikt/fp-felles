package no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal;

import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;

public class BehandleInngaaendeJournalSelftestConsumerImpl implements BehandleInngaaendeJournalSelftestConsumer {

    private BehandleInngaaendeJournalV1 port;
    private String endpointUrl;

    public BehandleInngaaendeJournalSelftestConsumerImpl(BehandleInngaaendeJournalV1 port, String endpointUrl) {
        this.port = port;
        this.endpointUrl = endpointUrl;
    }

    @Override
    public void ping() {
        port.ping();
    }

    @Override
    public String getEndpointUrl() {
        return endpointUrl;
    }
}
