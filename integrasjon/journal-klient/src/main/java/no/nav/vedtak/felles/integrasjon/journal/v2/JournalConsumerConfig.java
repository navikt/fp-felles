package no.nav.vedtak.felles.integrasjon.journal.v2;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.journal.v2.binding.JournalV2;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class JournalConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/journal/v2/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/journal/v2/Binding";
    private String endpointUrl;  // NOSONAR

    @Inject
    public JournalConsumerConfig(@KonfigVerdi("Journal_v2.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    JournalV2 getPort() {
        return ClientHelper.createServicePort(endpointUrl, JournalV2.class, WSDL, NAMESPACE,"Journal_v2", "Journal_v2Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
