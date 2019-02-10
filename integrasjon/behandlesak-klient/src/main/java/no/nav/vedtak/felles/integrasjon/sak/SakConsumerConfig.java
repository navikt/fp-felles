package no.nav.vedtak.felles.integrasjon.sak;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import no.nav.tjeneste.virksomhet.sak.v1.binding.SakV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class SakConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/sak/v1/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/sak/v1/Binding";

    @Inject
    @KonfigVerdi("Sak_v1.url")
    private String endpointUrl;  // NOSONAR

    SakV1 getPort() {
        return ClientHelper.createServicePort(endpointUrl, SakV1.class, WSDL, NAMESPACE,"Sak_v1", "Sak_v1Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
