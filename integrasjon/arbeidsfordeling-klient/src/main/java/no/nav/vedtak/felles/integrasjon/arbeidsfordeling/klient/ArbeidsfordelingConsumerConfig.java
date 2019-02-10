package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class ArbeidsfordelingConsumerConfig {
    private static final String ARBEIDSFORDELING_V_1_WSDL = "wsdl/no/nav/tjeneste/virksomhet/arbeidsfordeling/v1/Binding.wsdl";
    private static final String ARBEIDSFORDELING_V_1_NAMESPACE = "http://nav.no/tjeneste/virksomhet/arbeidsfordeling/v1/Binding";

    private String endpointUrl;

    @Inject
    public ArbeidsfordelingConsumerConfig(@KonfigVerdi("Arbeidsfordeling_v1.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    ArbeidsfordelingV1 getPort() {
        return ClientHelper.createServicePort(endpointUrl, ArbeidsfordelingV1.class, ARBEIDSFORDELING_V_1_WSDL, ARBEIDSFORDELING_V_1_NAMESPACE,"Arbeidsfordeling_v1", "Arbeidsfordeling_v1Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
