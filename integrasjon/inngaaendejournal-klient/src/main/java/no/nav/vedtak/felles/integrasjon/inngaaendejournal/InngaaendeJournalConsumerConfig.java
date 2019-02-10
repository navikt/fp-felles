package no.nav.vedtak.felles.integrasjon.inngaaendejournal;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
class InngaaendeJournalConsumerConfig {

    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/inngaaendeJournal/v1/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/inngaaendeJournal/v1/Binding";

    // TODO (u139158): Gjør verdi påkrevd når får innslag i Fasit
    @Inject
    @KonfigVerdi(value = "InngaaendeJournal_v1.url", required = false)
    private String endpointUrl; // NOSONAR

    InngaaendeJournalV1 getPort() {
        return ClientHelper.createServicePort(endpointUrl, InngaaendeJournalV1.class, WSDL, NAMESPACE,"InngaaendeJournal_v1", "InngaaendeJournal_v1Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
