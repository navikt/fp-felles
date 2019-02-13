package no.nav.vedtak.felles.integrasjon.journal.v3;

import no.nav.tjeneste.virksomhet.journal.v3.JournalV3;

class JournalSelftestConsumerImpl implements JournalSelftestConsumer {
    private JournalV3 port;
    private String endpointUrl;

    public JournalSelftestConsumerImpl(JournalV3 port, String endpointUrl) {
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
