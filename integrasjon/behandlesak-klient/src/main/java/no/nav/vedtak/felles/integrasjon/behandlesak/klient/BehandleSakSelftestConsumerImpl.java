package no.nav.vedtak.felles.integrasjon.behandlesak.klient;

import no.nav.tjeneste.virksomhet.behandlesak.v2.BehandleSakV2;

class BehandleSakSelftestConsumerImpl implements BehandleSakSelftestConsumer {
    private BehandleSakV2 port;
    private String endpointUrl;

    public BehandleSakSelftestConsumerImpl(BehandleSakV2 port, String endpointUrl) {
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
