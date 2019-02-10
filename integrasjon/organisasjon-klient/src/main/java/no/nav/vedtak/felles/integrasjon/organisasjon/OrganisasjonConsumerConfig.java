package no.nav.vedtak.felles.integrasjon.organisasjon;


import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class OrganisasjonConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/organisasjon/v4/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/organisasjon/v4/Binding";

    private String endpointUrl;  // NOSONAR

    @Inject
    public OrganisasjonConsumerConfig(@KonfigVerdi("Organisasjon_v4.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    OrganisasjonV4 getPort() {
        return ClientHelper.createServicePort(endpointUrl, OrganisasjonV4.class, WSDL, NAMESPACE,"Organisasjon_v4", "Organisasjon_v4Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
