package no.nav.vedtak.felles.integrasjon.behandlejournal;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;

public class BehandleJournalSelftestConsumerImpl implements BehandleJournalSelftestConsumer {

    private BehandleJournalV3 port;
    private String endpointUrl;

    public BehandleJournalSelftestConsumerImpl(BehandleJournalV3 port, String endpointUrl) {
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
