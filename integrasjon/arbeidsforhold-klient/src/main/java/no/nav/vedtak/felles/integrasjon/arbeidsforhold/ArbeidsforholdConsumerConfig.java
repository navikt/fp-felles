package no.nav.vedtak.felles.integrasjon.arbeidsforhold;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class ArbeidsforholdConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/arbeidsforhold/v3/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/arbeidsforhold/v3/Binding";

    private String endpointUrl;  // NOSONAR

    @Inject
    public ArbeidsforholdConsumerConfig(@KonfigVerdi("Arbeidsforhold_v3.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    ArbeidsforholdV3 getPort() {
        return ClientHelper.createServicePort(endpointUrl, ArbeidsforholdV3.class, WSDL, NAMESPACE,"Arbeidsforhold_v3", "Arbeidsforhold_v3Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
