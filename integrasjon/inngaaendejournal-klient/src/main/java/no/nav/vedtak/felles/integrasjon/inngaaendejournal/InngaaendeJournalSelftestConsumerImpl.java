package no.nav.vedtak.felles.integrasjon.inngaaendejournal;

import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1;

class InngaaendeJournalSelftestConsumerImpl implements InngaaendeJournalSelftestConsumer {

    private InngaaendeJournalV1 port;
    private String endpointUrl;

    public InngaaendeJournalSelftestConsumerImpl(InngaaendeJournalV1 port, String endpointUrl) {
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
