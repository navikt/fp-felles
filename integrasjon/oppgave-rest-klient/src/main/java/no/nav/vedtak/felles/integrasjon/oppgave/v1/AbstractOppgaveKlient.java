package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

//@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "oppgave.rs.uri", endpointDefault = "http://oppgave.default/api/v1/oppgaver",
//    scopesProperty = "oppgave.scopes", scopesDefault = "api://prod-fss.oppgavehandtering.oppgave/.default")
public abstract class AbstractOppgaveKlient implements Oppgaver {

    private static final String STATUSKATEGORI_AAPEN = "AAPEN";

    private final RestClient restKlient;
    private final RestConfig restConfig;

    protected AbstractOppgaveKlient() {
        this(RestClient.client());
    }

    protected AbstractOppgaveKlient(RestClient client) {
        this.restKlient = client;
        this.restConfig = RestConfig.forClient(this.getClass());
    }

    @Override
    public Oppgave opprettetOppgave(OpprettOppgave oppgave) {
        var request = RestRequest.newPOSTJson(oppgave, restConfig.endpoint(), restConfig)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return restKlient.send(request, Oppgave.class);
    }

    @Override
    public List<Oppgave> finnAlleOppgaver(String aktørId, String tema, List<String> oppgaveTyper) {
        var builder = UriBuilder.fromUri(restConfig.endpoint());
        if (aktørId != null) {
            builder.queryParam("aktoerId", aktørId);
        }
        if (tema != null) {
            builder.queryParam("tema", tema);
        }
        oppgaveTyper.forEach(ot -> builder.queryParam("oppgavetype", ot));
        var request = RestRequest.newGET(builder.build(), restConfig)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return restKlient.send(addCorrelation(request), FinnOppgaveResponse.class).oppgaver();
    }

    @Override
    public List<Oppgave> finnÅpneOppgaverForEnhet(String tema, List<String> oppgaveTyper, String tildeltEnhetsnr, String limit) {
        return hentOppgaverFor(null, tema, oppgaveTyper, tildeltEnhetsnr, limit);
    }

    @Override
    public List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper) {
        return hentOppgaverFor(aktørId, tema, oppgaveTyper, null, null);
    }

    private List<Oppgave> hentOppgaverFor(String aktørId, String tema, List<String> oppgaveTyper, String tildeltEnhetsnr, String limit) {
        var builder = UriBuilder.fromUri(restConfig.endpoint());
        if (aktørId != null) {
            builder.queryParam("aktoerId", aktørId);
        }
        if (tema != null) {
            builder.queryParam("tema", tema);
        }
        if (tildeltEnhetsnr != null) {
            builder.queryParam("tildeltEnhetsnr", tildeltEnhetsnr);
        }
        builder.queryParam("statuskategori", STATUSKATEGORI_AAPEN);
        //settes dersom man ønsker flere enn default limit på 10 fra api
        if (limit != null) {
            builder.queryParam("limit", limit);
        }
        oppgaveTyper.forEach(ot -> builder.queryParam("oppgavetype", ot));

        var request = RestRequest.newGET(builder.build(), restConfig)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return restKlient.send(addCorrelation(request), FinnOppgaveResponse.class).oppgaver();
    }

    @Override
    public void ferdigstillOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.id(), oppgave.versjon(), Oppgavestatus.FERDIGSTILT);
        var method = new RestRequest.Method(RestRequest.WebMethod.PATCH, RestRequest.jsonPublisher(patch));
        var request =  RestRequest.newRequest(method, getEndpointForOppgaveId(oppgaveId), restConfig)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        restKlient.sendExpectConflict(addCorrelation(request), String.class);
    }

    @Override
    public void feilregistrerOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.id(), oppgave.versjon(), Oppgavestatus.FEILREGISTRERT);
        var method = new RestRequest.Method(RestRequest.WebMethod.PATCH, RestRequest.jsonPublisher(patch));
        var request =  RestRequest.newRequest(method, getEndpointForOppgaveId(oppgaveId), restConfig)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        restKlient.sendExpectConflict(addCorrelation(request), String.class);
    }

    @Override
    public Oppgave hentOppgave(String oppgaveId) {
        var request = RestRequest.newGET(getEndpointForOppgaveId(oppgaveId), restConfig)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return restKlient.send(addCorrelation(request), Oppgave.class);
    }

    private URI getEndpointForOppgaveId(String oppgaveId) {
        return UriBuilder.fromUri(restConfig.endpoint()).path(oppgaveId).build();
    }

    private RestRequest addCorrelation(RestRequest request) {
        return request.otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
    }

}
