package no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag;

import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.InfotrygdBeregningsgrunnlagV1;

public class InfotrygdBeregningsgrunnlagSelftestConsumerImpl implements InfotrygdBeregningsgrunnlagSelftestConsumer {
    private InfotrygdBeregningsgrunnlagV1 port;
    private String endpointUrl;

    public InfotrygdBeregningsgrunnlagSelftestConsumerImpl(InfotrygdBeregningsgrunnlagV1 port, String endpointUrl) {
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