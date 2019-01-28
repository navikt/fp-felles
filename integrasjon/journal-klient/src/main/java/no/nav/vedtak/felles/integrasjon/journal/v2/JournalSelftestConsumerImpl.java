package no.nav.vedtak.felles.integrasjon.journal.v2;

import no.nav.tjeneste.virksomhet.journal.v2.binding.JournalV2;

class JournalSelftestConsumerImpl implements JournalSelftestConsumer {
    private JournalV2 port;
    private String endpointUrl;

    public JournalSelftestConsumerImpl(JournalV2 port, String endpointUrl) {
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
