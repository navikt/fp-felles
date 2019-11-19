package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import no.nav.vedtak.felles.integrasjon.oppgave.v1.request.OpprettOppgaveRequest;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.respons.Oppgave;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.Objects;

@ApplicationScoped
public class OppgaveConsumer {

    private OidcRestClient restClient;
    private URI baseUri;

    OppgaveConsumer() {
    }

    @Inject
    public OppgaveConsumer(OidcRestClient restClient, @KonfigVerdi("oppgave.url") URI endpoint) {
        this.restClient = restClient;
        this.baseUri = endpoint;
    }

    /**
     * Oppretter oppgave i Gosys
     *
     * @param request forespørselen
     * @return opprettede oppgaven
     */
    public Oppgave opprettOppgave(OpprettOppgaveRequest request) {
        Objects.requireNonNull(request);
        return restClient.post(baseUri, request, Oppgave.class);
    }
}
