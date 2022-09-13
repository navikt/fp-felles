package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.RestSender;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.log.mdc.MDCOperations;

@NativeClient
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, endpointProperty = "oppgave.rs.uri",
    endpointDefault = "http://oppgave.default/api/v1/oppgaver")
@ApplicationScoped
public class OppgaveNativeKlient implements Oppgaver {

    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    private static final String STATUSKATEGORI_AAPEN = "AAPEN";

    private RestSender restKlient;
    private URI endpoint;

    OppgaveNativeKlient() {
        // CDI proxyable
    }

    @Inject
    public OppgaveNativeKlient(RestSender restKlient) {
        this.restKlient = restKlient;
        this.endpoint = RestConfig.endpointFromAnnotation(OppgaveNativeKlient.class);
    }

    @Override
    public Oppgave opprettetOppgave(OpprettOppgave oppgave) {
        var request = lagRequest(endpoint).POST(RestRequest.jsonPublisher(oppgave));
        return restKlient.send(build(request), Oppgave.class);
    }

    @Override
    public List<Oppgave> finnAlleOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        var builder = UriBuilder.fromUri(endpoint).queryParam("aktoerId", aktørId);
        if (tema != null)
            builder.queryParam("tema", tema);
        oppgaveTyper.forEach(ot -> builder.queryParam("oppgavetype", ot));
        var request = lagRequest(builder.build()).GET();
        return restKlient.send(build(request), FinnOppgaveResponse.class).oppgaver();
    }

    @Override
    public List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        var builder = UriBuilder.fromUri(endpoint)
            .queryParam("aktoerId", aktørId)
            .queryParam("statuskategori", STATUSKATEGORI_AAPEN);
        if (tema != null)
            builder.queryParam("tema", tema);
        oppgaveTyper.forEach(ot -> builder.queryParam("oppgavetype", ot));
        var request = lagRequest(builder.build()).GET();
        return restKlient.send(build(request), FinnOppgaveResponse.class).oppgaver();
    }

    @Override
    public void ferdigstillOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.getId(), oppgave.getVersjon(), Oppgavestatus.FERDIGSTILT);
        var request = lagRequest(getEndpointForOppgaveId(oppgaveId))
            .method(RestRequest.patch(), RestRequest.jsonPublisher(patch));
        restKlient.sendExpectConflict(build(request));
    }

    @Override
    public void feilregistrerOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.getId(), oppgave.getVersjon(), Oppgavestatus.FEILREGISTRERT);
        var request = lagRequest(getEndpointForOppgaveId(oppgaveId))
            .method(RestRequest.patch(), RestRequest.jsonPublisher(patch));
        restKlient.sendExpectConflict(build(request));
    }

    @Override
    public Oppgave hentOppgave(String oppgaveId) {
        var request = lagRequest(getEndpointForOppgaveId(oppgaveId)).GET();
        return restKlient.send(build(request), Oppgave.class);
    }

    private URI getEndpointForOppgaveId(String oppgaveId) {
        return UriBuilder.fromUri(endpoint).path(oppgaveId).build();
    }

    private HttpRequest.Builder lagRequest(URI uri) {
        return HttpRequest.newBuilder(uri).header(HEADER_CORRELATION_ID, MDCOperations.getCallId());
    }

    private RestRequest build(HttpRequest.Builder builder) {
        return RestRequest.buildFor(OppgaveNativeKlient.class, builder);
    }

}
