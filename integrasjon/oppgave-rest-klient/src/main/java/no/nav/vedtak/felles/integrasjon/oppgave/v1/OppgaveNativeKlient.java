package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.NativeKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

@NativeKlient
@ApplicationScoped
public class OppgaveNativeKlient implements Oppgaver {

    private static final String ENDPOINT_KEY = "oppgave.rs.uri";
    private static final String DEFAULT_URI = "http://oppgave.default/api/v1/oppgaver";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    private static final String STATUSKATEGORI_AAPEN = "AAPEN";

    private RestKlient restKlient;
    private URI endpoint;

    OppgaveNativeKlient() {
        // CDI proxyable
    }

    @Inject
    public OppgaveNativeKlient(RestKlient restKlient,
                               @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.restKlient = restKlient;
        this.endpoint = endpoint;
    }

    @Override
    public Oppgave opprettetOppgave(OpprettOppgave oppgave) {
        var request = lagRequest().uri(endpoint).POST(RestRequest.serialiser(oppgave));
        return restKlient.send(request.build(), Oppgave.class);
    }

    @Override
    public List<Oppgave> finnAlleOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        var builder = UriBuilder.fromUri(endpoint).queryParam("aktoerId", aktørId);
        if (tema != null)
            builder.queryParam("tema", tema);
        oppgaveTyper.forEach(ot -> builder.queryParam("oppgavetype", ot));
        var request = lagRequest().uri(builder.build()).GET();
        return restKlient.send(request.build(), FinnOppgaveResponse.class).oppgaver();
    }

    @Override
    public List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        var builder = UriBuilder.fromUri(endpoint)
            .queryParam("aktoerId", aktørId)
            .queryParam("statuskategori", STATUSKATEGORI_AAPEN);
        if (tema != null)
            builder.queryParam("tema", tema);
        oppgaveTyper.forEach(ot -> builder.queryParam("oppgavetype", ot));
        var request = lagRequest().uri(builder.build()).GET();
        return restKlient.send(request.build(), FinnOppgaveResponse.class).oppgaver();
    }

    @Override
    public void ferdigstillOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.getId(), oppgave.getVersjon(), Oppgavestatus.FERDIGSTILT);
        var request = lagRequest().uri(getEndpointForOppgaveId(oppgaveId));
        RestRequest.patch(request, patch);
        restKlient.sendPermitConflict(request.build());
    }

    @Override
    public void feilregistrerOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.getId(), oppgave.getVersjon(), Oppgavestatus.FEILREGISTRERT);
        var request = lagRequest().uri(getEndpointForOppgaveId(oppgaveId));
        RestRequest.patch(request, patch);
        restKlient.sendPermitConflict(request.build());
    }

    @Override
    public Oppgave hentOppgave(String oppgaveId) {
        var request = lagRequest().uri(getEndpointForOppgaveId(oppgaveId)).GET();
        return restKlient.send(request.build(), Oppgave.class);
    }

    private URI getEndpointForOppgaveId(String oppgaveId) {
        return UriBuilder.fromUri(endpoint).path(oppgaveId).build();
    }

    private HttpRequest.Builder lagRequest() {
        return RestRequest.builder(SikkerhetContext.BRUKER)
            .header(HEADER_CORRELATION_ID, MDCOperations.getCallId());
    }

}
