package no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.MeldekortUtbetalingsgrunnlagV1;

public class MeldekortUtbetalingsgrunnlagSelftestConsumerImpl implements MeldekortUtbetalingsgrunnlagSelftestConsumer {
    private MeldekortUtbetalingsgrunnlagV1 port;
    private String endpointUrl;

    public MeldekortUtbetalingsgrunnlagSelftestConsumerImpl(MeldekortUtbetalingsgrunnlagV1 port, String endpointUrl) {
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