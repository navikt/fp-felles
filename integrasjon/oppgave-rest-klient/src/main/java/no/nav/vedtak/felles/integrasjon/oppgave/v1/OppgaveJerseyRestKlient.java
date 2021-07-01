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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey
public class OppgaveJerseyRestKlient extends AbstractJerseyOidcRestClient implements Oppgaver {

    private static final String ENDPOINT_KEY = "oppgave.rs.uri";
    private static final String DEFAULT_URI = "http://oppgave.default/api/v1/oppgaver";
    private static final String STATUSKATEGORI_AAPEN = "AAPEN";
    private static final Logger LOG = LoggerFactory.getLogger(OppgaveJerseyRestKlient.class);

    private final URI endpoint;

    @Inject
    public OppgaveJerseyRestKlient(@KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Oppgave opprettetOppgave(OpprettOppgave oppgave) {
            var target = client.target(endpoint);
            LOG.trace("Oppretter oppgave på {}", target.getUri());
            var res = invoke(target.request(APPLICATION_JSON_TYPE)
                    .buildPost(entity(oppgave, APPLICATION_JSON_TYPE)), Oppgave.class);
            LOG.info("Opprettet oppgave OK");
            return res;
    }

    @Override
    public List<Oppgave> finnAlleOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
            var b = client.target(endpoint)
                    .queryParam("aktoerId", aktørId)
                    .queryParam("oppgavetype", oppgaveTyper.toArray());
            if (tema != null) {
                b.queryParam("tema", tema);
            }
            LOG.trace("Henter alle oppgaver fra {}", b.getUri());
            var res = invoke(b.request(APPLICATION_JSON).buildGet(), FinnOppgaveResponse.class).oppgaver();
            LOG.info("Henter alle oppgaver OK");
            return res;
    }

    @Override
    public List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
            var b = client.target(endpoint)
                    .queryParam("aktoerId", aktørId)
                    .queryParam("statuskategori", STATUSKATEGORI_AAPEN)
                    .queryParam("oppgavetype", oppgaveTyper.toArray());
            if (tema != null) {
                b.queryParam("tema", tema);
            }
            LOG.trace("Henter åpne oppgaver fra {}", b.getUri());
            var res = invoke(b.request(APPLICATION_JSON)
                    .header(HEADER_CORRELATION_ID, getCallId()).buildGet(),FinnOppgaveResponse.class).oppgaver();
            
            LOG.info("Henter alle åpne oppgaver OK");
            return res;
    }

    @Override
    public void ferdigstillOppgave(String oppgaveId) {
            var uri = getEndpointForOppgaveId(oppgaveId);
            LOG.trace("Ferdigstiller oppgave fra {}", uri);
            patch(uri, patchOppgave(hentOppgave(oppgaveId), FERDIGSTILT));
            LOG.info("Ferdigstilt oppgave OK");
    }

    @Override
    public void feilregistrerOppgave(String oppgaveId) {
            var uri = getEndpointForOppgaveId(oppgaveId);
            LOG.trace("feilregistrerer oppgave fra {}", uri);
            patch(getEndpointForOppgaveId(oppgaveId), patchOppgave(hentOppgave(oppgaveId), FEILREGISTRERT));
            LOG.info("feilregistrerer oppgave OK");
    }

    @Override
    public Oppgave hentOppgave(String oppgaveId) {
            var target = client.target(endpoint);
            LOG.trace("Henter oppgave fra {}", target.getUri());
            var res = invoke(target
                    .path(oppgaveId)
                    .request(APPLICATION_JSON).buildGet(), Oppgave.class);
            LOG.info("Hentet oppgave fra {} OK", target.getUri());
            return res;
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

}
