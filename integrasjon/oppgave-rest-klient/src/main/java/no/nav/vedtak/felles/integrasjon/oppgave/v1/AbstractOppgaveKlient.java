package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * API / Swagger https://oppgave.dev.intern.nav.no
 * <p>
 * Bruk TokenFlow ADAPTIVE og scope cluster:oppgavehandtering.oppgave mote /api/v1/oppgaver
 */
public abstract class AbstractOppgaveKlient implements Oppgaver {

    private static final String STATUSKATEGORI_AAPEN = "AAPEN";
    private static final String TEMA_FORELDREPENGER = "FOR";

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
    public List<Oppgave> finnÅpneOppgaverAvType(Oppgavetype oppgaveType, String aktørId, String enhetsNr, String limit) {
        Objects.requireNonNull(oppgaveType, "Oppgvetype er påkrevd");
        return hentOppgaverFor(aktørId, List.of(oppgaveType.getKode()), enhetsNr, limit);
    }

    @Override
    public List<Oppgave> finnÅpneOppgaver(List<String> oppgaveTyper, String aktørId, String enhetsNr, String limit) {
        return hentOppgaverFor(aktørId, oppgaveTyper, enhetsNr, limit);
    }

    private List<Oppgave> hentOppgaverFor(String aktørId, List<String> oppgaveTyper, String tildeltEnhetsnr, String limit) {
        var builder = UriBuilder.fromUri(restConfig.endpoint())
            .queryParam("tema", AbstractOppgaveKlient.TEMA_FORELDREPENGER)
            .queryParam("statuskategori", STATUSKATEGORI_AAPEN);

        if (aktørId != null) {
            builder.queryParam("aktoerId", aktørId);
        }
        if (tildeltEnhetsnr != null) {
            builder.queryParam("tildeltEnhetsnr", tildeltEnhetsnr);
        }
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
        var request = RestRequest.newRequest(method, getEndpointForOppgaveId(oppgaveId), restConfig)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        restKlient.sendExpectConflict(addCorrelation(request), String.class);
    }

    @Override
    public void feilregistrerOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.id(), oppgave.versjon(), Oppgavestatus.FEILREGISTRERT);
        var method = new RestRequest.Method(RestRequest.WebMethod.PATCH, RestRequest.jsonPublisher(patch));
        var request = RestRequest.newRequest(method, getEndpointForOppgaveId(oppgaveId), restConfig)
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
