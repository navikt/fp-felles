package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1;

class ArbeidsfordelingSelftestConsumerImpl implements ArbeidsfordelingSelftestConsumer {
    private ArbeidsfordelingV1 port;
    private String endpointUrl;

    public ArbeidsfordelingSelftestConsumerImpl(ArbeidsfordelingV1 port, String endpointUrl) {
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
