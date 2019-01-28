package no.nav.vedtak.felles.integrasjon.infotrygdsak;

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.InfotrygdSakV1;

public class InfotrygdSakSelftestConsumerImpl implements InfotrygdSakSelftestConsumer {
    private InfotrygdSakV1 port;
    private String endpointUrl;

    public InfotrygdSakSelftestConsumerImpl(InfotrygdSakV1 port, String endpointUrl) {
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