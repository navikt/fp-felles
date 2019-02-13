package no.nav.vedtak.felles.integrasjon.behandleoppgave;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;

class BehandleoppgaveSelftestConsumerImpl implements BehandleoppgaveSelftestConsumer {
    private BehandleOppgaveV1 port;
    private String endpointUrl;

    public BehandleoppgaveSelftestConsumerImpl(BehandleOppgaveV1 port, String endpointUrl) {
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
