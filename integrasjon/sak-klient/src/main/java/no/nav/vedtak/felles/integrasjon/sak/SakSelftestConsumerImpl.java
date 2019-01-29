package no.nav.vedtak.felles.integrasjon.sak;

import no.nav.tjeneste.virksomhet.sak.v1.binding.SakV1;

class SakSelftestConsumerImpl implements SakSelftestConsumer {
    private SakV1 port;
    private String endpointUrl;

    public SakSelftestConsumerImpl(SakV1 port, String endpointUrl) {
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
