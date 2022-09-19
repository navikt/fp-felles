package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@NativeClient
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, endpointProperty = "oppgave.rs.uri",
    endpointDefault = "http://oppgave.default/api/v1/oppgaver")
@ApplicationScoped
public class OppgaveNativeKlient implements Oppgaver {

    private static final String STATUSKATEGORI_AAPEN = "AAPEN";

    private RestClient restKlient;
    private URI endpoint;

    OppgaveNativeKlient() {
        // CDI proxyable
    }

    @Inject
    public OppgaveNativeKlient(RestClient restKlient) {
        this.restKlient = restKlient;
        this.endpoint = RestConfig.endpointFromAnnotation(OppgaveNativeKlient.class);
    }

    @Override
    public Oppgave opprettetOppgave(OpprettOppgave oppgave) {
        var request = RestRequest.newPOSTJson(oppgave, endpoint, OppgaveNativeKlient.class)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return restKlient.send(request, Oppgave.class);
    }

    @Override
    public List<Oppgave> finnAlleOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        var builder = UriBuilder.fromUri(endpoint).queryParam("aktoerId", aktørId);
        if (tema != null)
            builder.queryParam("tema", tema);
        oppgaveTyper.forEach(ot -> builder.queryParam("oppgavetype", ot));
        var request = RestRequest.newGET(builder.build(), OppgaveNativeKlient.class)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return restKlient.send(addCorrelation(request), FinnOppgaveResponse.class).oppgaver();
    }

    @Override
    public List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        var builder = UriBuilder.fromUri(endpoint)
            .queryParam("aktoerId", aktørId)
            .queryParam("statuskategori", STATUSKATEGORI_AAPEN);
        if (tema != null)
            builder.queryParam("tema", tema);
        oppgaveTyper.forEach(ot -> builder.queryParam("oppgavetype", ot));
        var request = RestRequest.newGET(builder.build(), OppgaveNativeKlient.class)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return restKlient.send(addCorrelation(request), FinnOppgaveResponse.class).oppgaver();
    }

    @Override
    public void ferdigstillOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.getId(), oppgave.getVersjon(), Oppgavestatus.FERDIGSTILT);
        var method = new RestRequest.Method(RestRequest.WebMethod.PATCH, RestRequest.jsonPublisher(patch));
        var request =  RestRequest.newRequest(method, getEndpointForOppgaveId(oppgaveId), OppgaveNativeKlient.class)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        restKlient.sendExpectConflict(addCorrelation(request), String.class);
    }

    @Override
    public void feilregistrerOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.getId(), oppgave.getVersjon(), Oppgavestatus.FEILREGISTRERT);
        var method = new RestRequest.Method(RestRequest.WebMethod.PATCH, RestRequest.jsonPublisher(patch));
        var request =  RestRequest.newRequest(method, getEndpointForOppgaveId(oppgaveId), OppgaveNativeKlient.class)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        restKlient.sendExpectConflict(addCorrelation(request), String.class);
    }

    @Override
    public Oppgave hentOppgave(String oppgaveId) {
        var request = RestRequest.newGET(getEndpointForOppgaveId(oppgaveId), OppgaveNativeKlient.class)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return restKlient.send(addCorrelation(request), Oppgave.class);
    }

    private URI getEndpointForOppgaveId(String oppgaveId) {
        return UriBuilder.fromUri(endpoint).path(oppgaveId).build();
    }

    private RestRequest addCorrelation(RestRequest request) {
        return request.otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
    }

}
