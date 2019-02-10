package no.nav.vedtak.felles.integrasjon.kodeverk;


import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class KodeverkConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/kodeverk/v2/Kodeverk.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/kodeverk/v2/";

    private String endpointUrl;  // NOSONAR

    @Inject
    public KodeverkConsumerConfig(@KonfigVerdi("Kodeverk_v2.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    KodeverkPortType getPort() {
        return ClientHelper.createServicePort(endpointUrl, KodeverkPortType.class, WSDL, NAMESPACE,"Kodeverk_v2", "Kodeverk_v2");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
