package no.nav.vedtak.felles.integrasjon.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;

public class KodeverkSelftestConsumerImpl implements KodeverkSelftestConsumer {
    private KodeverkPortType port;
    private String endpointUrl;

    public KodeverkSelftestConsumerImpl(KodeverkPortType port, String endpointUrl) {
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
