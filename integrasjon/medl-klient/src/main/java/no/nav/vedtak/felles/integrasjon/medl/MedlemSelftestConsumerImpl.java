package no.nav.vedtak.felles.integrasjon.medl;

import no.nav.tjeneste.virksomhet.medlemskap.v2.MedlemskapV2;

public class MedlemSelftestConsumerImpl implements MedlemSelftestConsumer {
    private MedlemskapV2 port;
    private String endpointUrl;

    public MedlemSelftestConsumerImpl(MedlemskapV2 port, String endpointUrl) {
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
