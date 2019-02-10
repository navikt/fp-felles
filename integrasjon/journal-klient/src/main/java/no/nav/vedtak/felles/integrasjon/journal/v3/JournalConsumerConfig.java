package no.nav.vedtak.felles.integrasjon.journal.v3;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.journal.v3.JournalV3;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class JournalConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/journal/v3/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/journal/v3/Binding";
    private String endpointUrl;  // NOSONAR

    @Inject
    public JournalConsumerConfig(@KonfigVerdi("Journal_v3.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    JournalV3 getPort() {
        return ClientHelper.createServicePort(endpointUrl, JournalV3.class, WSDL, NAMESPACE,"Journal_v3", "Journal_v3Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
