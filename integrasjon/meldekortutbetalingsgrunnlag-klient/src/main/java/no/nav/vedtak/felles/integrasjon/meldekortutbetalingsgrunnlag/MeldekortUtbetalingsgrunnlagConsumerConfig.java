package no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.MeldekortUtbetalingsgrunnlagV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class MeldekortUtbetalingsgrunnlagConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/Binding";

    private String endpointUrl;

    @Inject
    public MeldekortUtbetalingsgrunnlagConsumerConfig(@KonfigVerdi("MeldekortUtbetalingsgrunnlag_v1.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    MeldekortUtbetalingsgrunnlagV1 getPort() {
        return ClientHelper.createServicePort(endpointUrl, MeldekortUtbetalingsgrunnlagV1.class, WSDL, NAMESPACE,"MeldekortUtbetalingsgrunnlag_v1", "meldekortUtbetalingsgrunnlag_v1Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
