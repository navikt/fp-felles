package no.nav.vedtak.felles.integrasjon.medl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.medlemskap.v2.MedlemskapV2;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class MedlemConsumerConfig {

    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/medlemskap/v2/MedlemskapV2.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/medlemskap/v2";

    private String endpointUrl;  // NOSONAR

    @Inject
    public MedlemConsumerConfig(@KonfigVerdi("Medlem_v2.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    MedlemskapV2 getPort() {
        return ClientHelper.createServicePort(endpointUrl, MedlemskapV2.class, WSDL, NAMESPACE,"Medlemskap_v2", "Medlemskap_v2Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
