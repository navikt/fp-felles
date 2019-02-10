package no.nav.vedtak.felles.integrasjon.oppgave;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.oppgave.v3.binding.OppgaveV3;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class OppgaveConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/oppgave/v3/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/oppgave/v3/Binding";

    private String endpointUrl;  // NOSONAR

    @Inject
    public OppgaveConsumerConfig(@KonfigVerdi("Oppgave_v3.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    OppgaveV3 getPort() {
        return ClientHelper.createServicePort(endpointUrl, OppgaveV3.class, WSDL, NAMESPACE,"Oppgave_v3", "Oppgave_v3Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
