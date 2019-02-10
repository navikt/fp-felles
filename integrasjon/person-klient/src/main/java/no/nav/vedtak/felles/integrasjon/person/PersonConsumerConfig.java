package no.nav.vedtak.felles.integrasjon.person;


import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class PersonConsumerConfig {
    private static final String PERSON_V3_WSDL = "wsdl/no/nav/tjeneste/virksomhet/person/v3/Binding.wsdl";
    private static final String PERSON_V3_NAMESPACE = "http://nav.no/tjeneste/virksomhet/person/v3/Binding";

    private String endpointUrl; // NOSONAR

    @Inject
    public PersonConsumerConfig(@KonfigVerdi("Person_v3.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    PersonV3 getPort() {
        return ClientHelper.createServicePort(endpointUrl, PersonV3.class, PERSON_V3_WSDL, PERSON_V3_NAMESPACE,"Person_v3", "Person_v3Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
