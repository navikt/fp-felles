package no.nav.vedtak.felles.integrasjon.oppgave;

import no.nav.tjeneste.virksomhet.oppgave.v3.binding.OppgaveV3;

class OppgaveSelftestConsumerImpl implements OppgaveSelftestConsumer {
    private OppgaveV3 port;
    private String endpointUrl;

    public OppgaveSelftestConsumerImpl(OppgaveV3 port, String endpointUrl) {
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
