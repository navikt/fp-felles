package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Jersey
public class ArbeidsfordelingJerseyRestKlient extends AbstractJerseyOidcRestClient implements Arbeidsfordeling {

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsfordelingJerseyRestKlient.class);
    private static final String DEFAULT_URI = "https://app.adeo.no/norg2/api/v1/arbeidsfordeling/enheter";
    private static final String BEST_MATCH = "/bestmatch";

    private URI uri;

    public ArbeidsfordelingJerseyRestKlient() {

    }

    public ArbeidsfordelingJerseyRestKlient(
            @KonfigVerdi(value = "arbeidsfordeling.rs.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.uri = uri;
        LOG.info("Konstruert " + this);
    }

    @Override
    public List<ArbeidsfordelingResponse> hentAlleAktiveEnheter(ArbeidsfordelingRequest request) {
        return hentEnheterFor(request, null);
    }

    @Override
    public List<ArbeidsfordelingResponse> finnEnhet(ArbeidsfordelingRequest req) {
        return hentEnheterFor(req, BEST_MATCH);
    }

    private List<ArbeidsfordelingResponse> hentEnheterFor(ArbeidsfordelingRequest req, String path) {
        try {
            var target = target(path);
            LOG.info("henter enheter fra {}", target.getUri());
            return target
                    .request()
                    .accept(APPLICATION_JSON_TYPE)
                    .buildPost(json(req))
                    .invoke(new GenericType<List<ArbeidsfordelingResponse>>() {
                    }).stream()
                    .filter(r -> "AKTIV".equalsIgnoreCase(r.getStatus()))
                    .collect(toList());
        } catch (WebApplicationException e) {
            throw new IntegrasjonException("F-016913", e, uri, e.getResponse().getStatus(), e.getResponse().getEntity());
        } catch (Exception e) {
            LOG.warn("Henting av enheter feilet", e);
            throw new IntegrasjonException(e, uri);
        }
    }

    private WebTarget target(String path) {
        LOG.info("TARGET " + uri);
        var target = client.target(uri);
        return Optional.ofNullable(path)
                .map(p -> target.path(p))
                .orElse(target);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [uri=" + uri + "]";
    }
}
