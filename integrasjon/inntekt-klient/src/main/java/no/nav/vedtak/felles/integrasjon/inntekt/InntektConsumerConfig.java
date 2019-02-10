package no.nav.vedtak.felles.integrasjon.inntekt;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.inntekt.v3.binding.InntektV3;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class InntektConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/inntekt/v3/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/inntekt/v3/Binding";

    private String endpointUrl;  // NOSONAR

    @Inject
    public InntektConsumerConfig(@KonfigVerdi("Inntekt_v3.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    InntektV3 getPort() {
        return ClientHelper.createServicePort(endpointUrl, InntektV3.class, WSDL, NAMESPACE,"Inntekt_v3", "Inntekt_v3Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
