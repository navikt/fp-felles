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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Jersey
public class OppgaveJerseyRestKlient extends AbstractJerseyOidcRestClient implements Oppgaver {

    private static final String ENDPOINT_KEY = "oppgave.rs.uri";
    private static final String DEFAULT_URI = "http://oppgave.default/api/v1/oppgaver";
    private static final String STATUSKATEGORI_AAPEN = "AAPEN";
    private static final Logger LOG = LoggerFactory.getLogger(OppgaveJerseyRestKlient.class);

    private URI endpoint;

    public OppgaveJerseyRestKlient() {
    }

    @Inject
    public OppgaveJerseyRestKlient(@KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        super();
        this.endpoint = endpoint;
    }

    @Override
    public Oppgave opprettetOppgave(OpprettOppgave.Builder requestBuilder) {
        try {
            var target = client.target(endpoint);
            LOG.info("Oppretter oppgave på {}", target.getUri());
            var res = target.request(APPLICATION_JSON_TYPE)
                    .buildPost(entity(requestBuilder.build(), APPLICATION_JSON_TYPE))
                    .invoke(Oppgave.class);
            LOG.info("Opprettet oppgave OK");
            return res;
        } catch (Exception e) {
            LOG.warn("Kunne ikke opprette oppgave", e);
            throw e;
        }
    }

    @Override
    public List<Oppgave> finnAlleOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        try {
            var b = client.target(endpoint)
                    .queryParam("aktoerId", aktørId)
                    .queryParam("oppgavetype", oppgaveTyper.toArray());
            if (tema != null) {
                b.queryParam("tema", tema);
            }
            LOG.info("Henter alle oppgaver fra {}", b.getUri());
            var res = b.request(APPLICATION_JSON)
                    .get(FinnOppgaveResponse.class).getOppgaver();
            LOG.info("Henter alle oppgaver OK");
            return res;
        } catch (Exception e) {
            LOG.warn("Kunne ikke hente alle oppgaver", e);
            throw e;
        }
    }

    @Override
    public List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        try {
            var b = client.target(endpoint)
                    .queryParam("aktoerId", aktørId)
                    .queryParam("statuskategori", STATUSKATEGORI_AAPEN)
                    .queryParam("oppgavetype", oppgaveTyper.toArray());
            if (tema != null) {
                b.queryParam("tema", tema);
            }
            LOG.info("Henter åpne oppgaver fra {}", b.getUri());
            var res = b.request(APPLICATION_JSON)
                    .header(HEADER_CORRELATION_ID, getCallId()).get(FinnOppgaveResponse.class).getOppgaver();
            LOG.info("Henter alle åpne oppgaver OK");
            return res;
        } catch (Exception e) {
            LOG.warn("Kunne ikke hente åpne oppgaver", e);
            throw e;
        }
    }

    @Override
    public void ferdigstillOppgave(String oppgaveId) {
        try {
            var uri = getEndpointForOppgaveId(oppgaveId);
            LOG.info("Ferdigstiller oppgave fra {}", uri);
            patch(uri, patchOppgave(hentOppgave(oppgaveId), FERDIGSTILT));
        } catch (Exception e) {
            LOG.warn("Kunne ikke ferdigstille oppgave", e);
            throw e;
        }
    }

    @Override
    public void feilregistrerOppgave(String oppgaveId) {
        try {
            var uri = getEndpointForOppgaveId(oppgaveId);
            LOG.info("feilregistrerer oppgave fra {}", uri);
            patch(getEndpointForOppgaveId(oppgaveId), patchOppgave(hentOppgave(oppgaveId), FEILREGISTRERT));
        } catch (Exception e) {
            LOG.warn("Kunne ikke feilregistrerer oppgave", e);
            throw e;
        }
    }

    @Override
    public Oppgave hentOppgave(String oppgaveId) {
        try {
            var target = client.target(endpoint);
            LOG.info("Henter oppgave fra {}", target.getUri());
            var res = target
                    .path(oppgaveId)
                    .request(APPLICATION_JSON)
                    .get(Oppgave.class);
            return res;
        } catch (Exception e) {
            LOG.warn("Kunne ikke hente oppgave", e);
            throw e;
        }
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
