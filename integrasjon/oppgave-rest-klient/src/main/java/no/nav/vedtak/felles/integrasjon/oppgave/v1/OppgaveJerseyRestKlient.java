package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavestatus.FEILREGISTRERT;
import static no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavestatus.FERDIGSTILT;
import static no.nav.vedtak.log.mdc.MDCOperations.getCallId;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.log.mdc.MDCOperations;

//@ApplicationScoped
public class OppgaveJerseyRestKlient extends AbstractJerseyOidcRestClient {

    private static final String ENDPOINT_KEY = "oppgave.rs.uri";
    private static final String DEFAULT_URI = "http://oppgave.default/api/v1/oppgaver";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    private static final String STATUSKATEGORI_AAPEN = "AAPEN";

    private URI endpoint;

    public OppgaveJerseyRestKlient() {
    }

    @Inject
    public OppgaveJerseyRestKlient(@KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.endpoint = endpoint;
    }

    public Oppgave opprettetOppgave(OpprettOppgave.Builder requestBuilder) {
        return client.target(endpoint)
                .request(APPLICATION_JSON_TYPE)
                .header(HEADER_CORRELATION_ID, getCallId())
                .buildPost(entity(requestBuilder.build(), APPLICATION_JSON_TYPE))
                .invoke(Oppgave.class);
    }

    public List<Oppgave> finnAlleOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        var b = client.target(endpoint)
                .queryParam("aktoerId", aktørId)
                .queryParam("oppgavetype", oppgaveTyper.toArray());
        if (tema != null) {
            b.queryParam("tema", tema);
        }
        return b.request(APPLICATION_JSON)
                .header(HEADER_CORRELATION_ID, MDCOperations.getCallId()).get(FinnOppgaveResponse.class).getOppgaver();
    }

    public List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        var b = client.target(endpoint)
                .queryParam("aktoerId", aktørId)
                .queryParam("statuskategori", STATUSKATEGORI_AAPEN)
                .queryParam("oppgavetype", oppgaveTyper.toArray());
        if (tema != null) {
            b.queryParam("tema", tema);
        }
        return b.request(APPLICATION_JSON)
                .header(HEADER_CORRELATION_ID, getCallId()).get(FinnOppgaveResponse.class).getOppgaver();
    }

    public void ferdigstillOppgave(String oppgaveId) {
        patch(getEndpointForOppgaveId(oppgaveId), patchOppgave(hentOppgave(oppgaveId), FERDIGSTILT), headers());
    }

    public void feilregistrerOppgave(String oppgaveId) {
        patch(getEndpointForOppgaveId(oppgaveId), patchOppgave(hentOppgave(oppgaveId), FEILREGISTRERT), headers());
    }

    public Oppgave hentOppgave(String oppgaveId) {
        return client
                .target(endpoint)
                .path(oppgaveId)
                .request(APPLICATION_JSON)
                .header(HEADER_CORRELATION_ID, getCallId())
                .get(Oppgave.class);
    }

    private PatchOppgave patchOppgave(Oppgave oppgave, Oppgavestatus status) {
        return new PatchOppgave(oppgave.getId(), oppgave.getVersjon(), status);
    }

    private URI getEndpointForOppgaveId(String oppgaveId) {
        try {
            return new URIBuilder(endpoint)
                    .setPath(oppgaveId).build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Set<Header> headers() {
        return Set.of(new BasicHeader(HEADER_CORRELATION_ID, MDCOperations.getCallId()));
    }
}
