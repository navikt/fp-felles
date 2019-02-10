package no.nav.vedtak.felles.integrasjon.behandlejournal;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class BehandleJournalConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/behandleJournal/v3/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/behandleJournal/v3/Binding";

    private String endpointUrl; // NOSONAR

    @Inject
    public BehandleJournalConsumerConfig(@KonfigVerdi("BehandleJournal_v3.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    BehandleJournalV3 getPort() {
        return ClientHelper.createServicePort(endpointUrl, BehandleJournalV3.class, WSDL, NAMESPACE,"BehandleJournal_v3", "behandleJournal_v3Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
