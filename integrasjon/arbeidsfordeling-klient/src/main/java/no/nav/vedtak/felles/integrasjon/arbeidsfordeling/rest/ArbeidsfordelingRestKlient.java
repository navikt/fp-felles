package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@ApplicationScoped
public class ArbeidsfordelingRestKlient implements Arbeidsfordeling {

    private static final String DEFAULT_URI = "https://app.adeo.no/norg2/api/v1/arbeidsfordeling/enheter";
    private static final String BEST_MATCH = "/bestmatch";

    private OidcRestClient restClient;
    private URI alleEnheterUri;
    private URI besteEnhetUri;

    @Inject
    public ArbeidsfordelingRestKlient(OidcRestClient restClient,
                                      @KonfigVerdi(value = "arbeidsfordeling.rs.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.restClient = restClient;
        this.alleEnheterUri = uri;
        this.besteEnhetUri = URI.create(uri + BEST_MATCH);
    }

    ArbeidsfordelingRestKlient() {
    }

    @Override
    public List<ArbeidsfordelingResponse> hentAlleAktiveEnheter(ArbeidsfordelingRequest request) {
        return hentEnheterFor(request, alleEnheterUri);
    }

    @Override
    public List<ArbeidsfordelingResponse> finnEnhet(ArbeidsfordelingRequest request) {
        return hentEnheterFor(request, besteEnhetUri);
    }

    private List<ArbeidsfordelingResponse> hentEnheterFor(ArbeidsfordelingRequest request, URI uri) {
        try {
            var respons = restClient.post(uri, request, ArbeidsfordelingResponse[].class);
            return Arrays.stream(respons)
                .filter(response -> "AKTIV".equalsIgnoreCase(response.status()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IntegrasjonException("F-016913", String.format("NORG2 arbeidsfordeling feil ved oppslag mot %s", uri), e);
        }
    }

}
