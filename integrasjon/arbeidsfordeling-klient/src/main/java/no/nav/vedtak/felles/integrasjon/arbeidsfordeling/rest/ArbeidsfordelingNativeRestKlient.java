package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.NativeKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

@NativeKlient
@ApplicationScoped
public class ArbeidsfordelingNativeRestKlient implements Arbeidsfordeling {

    private static final String DEFAULT_URI = "https://app.adeo.no/norg2/api/v1/arbeidsfordeling/enheter";
    private static final String BEST_MATCH = "/bestmatch";

    private RestKlient restKlient;
    private URI alleEnheterUri;
    private URI besteEnhetUri;

    @Inject
    public ArbeidsfordelingNativeRestKlient(RestKlient restKlient,
                                            @KonfigVerdi(value = "arbeidsfordeling.rs.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.restKlient = restKlient;
        this.alleEnheterUri = uri;
        this.besteEnhetUri = URI.create(uri + BEST_MATCH);
    }

    ArbeidsfordelingNativeRestKlient() {
        // CDI proxyable
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
            var httpRequest = restKlient.request().builder(SikkerhetContext.BRUKER)
                .uri(uri)
                .POST(RestRequest.serialiser(request))
                .build();
            var respons = restKlient.send(httpRequest, ArbeidsfordelingResponse[].class);
            return Arrays.stream(respons)
                .filter(response -> "AKTIV".equalsIgnoreCase(response.status()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IntegrasjonException("F-016913", String.format("NORG2 arbeidsfordeling feil ved oppslag mot %s", uri), e);
        }
    }

}
