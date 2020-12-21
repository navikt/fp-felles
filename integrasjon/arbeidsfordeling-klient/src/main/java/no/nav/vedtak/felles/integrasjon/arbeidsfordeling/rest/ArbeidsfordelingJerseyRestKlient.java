package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOIDCRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

public class ArbeidsfordelingJerseyRestKlient extends AbstractJerseyOIDCRestClient {

    private static final String DEFAULT_URI = "https://app.adeo.no/norg2/api/v1/arbeidsfordeling/enheter";
    private static final String BEST_MATCH = "/bestmatch";

    private final URI uri;

    public ArbeidsfordelingJerseyRestKlient(
            @KonfigVerdi(value = "arbeidsfordeling.rs.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.uri = uri;
    }

    public List<ArbeidsfordelingResponse> hentAlleAktiveEnheter(ArbeidsfordelingRequest request) {
        return hentEnheterFor(request, null);
    }

    public List<ArbeidsfordelingResponse> finnEnhet(ArbeidsfordelingRequest request) {
        return hentEnheterFor(request, BEST_MATCH);
    }

    private List<ArbeidsfordelingResponse> hentEnheterFor(ArbeidsfordelingRequest request, String path) {
        try {
            return target(path)
                    .request()
                    .accept(APPLICATION_JSON_TYPE)
                    .get(Response.class)
                    .readEntity(new GenericType<List<ArbeidsfordelingResponse>>() {
                    }).stream()
                    .filter(r -> "AKTIV".equalsIgnoreCase(r.getStatus()))
                    .collect(toList());
        } catch (WebApplicationException e) {
            throw new IntegrasjonException("F-016913", e, uri, e.getResponse().getStatus(), e.getResponse().getEntity());
        } catch (Exception e) {
            throw new IntegrasjonException(e, uri);
        }
    }

    private WebTarget target(String path) {
        var target = client.target(uri);
        return Optional.ofNullable(path)
                .map(p -> target.path(p))
                .orElse(target);
    }
}
