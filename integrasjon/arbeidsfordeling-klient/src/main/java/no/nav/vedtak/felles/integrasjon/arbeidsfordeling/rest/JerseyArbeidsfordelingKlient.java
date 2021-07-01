package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey
public class JerseyArbeidsfordelingKlient extends AbstractJerseyOidcRestClient implements Arbeidsfordeling {

    private static final String AKTIV = "AKTIV";
    private static final Logger LOG = LoggerFactory.getLogger(JerseyArbeidsfordelingKlient.class);
    private static final String DEFAULT_URI = "https://app.adeo.no/norg2/api/v1/arbeidsfordeling/enheter";
    private static final String BEST_MATCH = "/bestmatch";

    private final URI uri;

    @Inject
    public JerseyArbeidsfordelingKlient(@KonfigVerdi(value = "arbeidsfordeling.rs.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.uri = uri;
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
                    .filter(r -> AKTIV.equalsIgnoreCase(r.status()))
                    .collect(toList());
        } catch (Exception e) {
            LOG.warn("Henting av enheter feilet", e);
            throw new IntegrasjonException("F-016913", String.format("Henting av enheter fra %s feilet", uri));
        }
    }

    private WebTarget target(String path) {
        var target = client.target(uri);
        return Optional.ofNullable(path)
                .map(target::path)
                .orElseGet(() -> target);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [uri=" + uri + "]";
    }
}
