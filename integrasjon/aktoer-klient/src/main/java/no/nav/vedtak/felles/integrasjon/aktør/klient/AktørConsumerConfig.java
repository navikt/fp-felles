package no.nav.vedtak.felles.integrasjon.aktør.klient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class AktørConsumerConfig {
    private static final String AKTOER_V_2_WSDL = "wsdl/no/nav/tjeneste/virksomhet/aktoer/v2/Binding.wsdl";
    private static final String AKTOER_V_2_NAMESPACE = "http://nav.no/tjeneste/virksomhet/aktoer/v2/Binding/";

    private String endpointUrl;

    @Inject
    public AktørConsumerConfig(@KonfigVerdi("Aktoer_v2.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    AktoerV2 getPort() {
        return ClientHelper.createServicePort(endpointUrl, AktoerV2.class, AKTOER_V_2_WSDL, AKTOER_V_2_NAMESPACE,"Aktoer", "Aktoer_v2Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
