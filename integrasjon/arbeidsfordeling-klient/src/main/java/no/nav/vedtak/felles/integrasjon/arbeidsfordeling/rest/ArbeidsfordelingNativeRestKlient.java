package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestCompact;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@NativeClient
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, endpointProperty = "arbeidsfordeling.rs.url",
    endpointDefault = "https://app.adeo.no/norg2/api/v1/arbeidsfordeling/enheter")
@ApplicationScoped
public class ArbeidsfordelingNativeRestKlient implements Arbeidsfordeling {

    private static final String BEST_MATCH = "/bestmatch";

    private RestCompact restKlient;
    private URI alleEnheterUri;
    private URI besteEnhetUri;

    @Inject
    public ArbeidsfordelingNativeRestKlient(RestCompact restKlient) {
        this.restKlient = restKlient;
        this.alleEnheterUri = RestConfig.endpointFromAnnotation(ArbeidsfordelingNativeRestKlient.class);
        this.besteEnhetUri = URI.create(alleEnheterUri + BEST_MATCH);
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
            var respons = restKlient.postValue(ArbeidsfordelingNativeRestKlient.class, uri, request, ArbeidsfordelingResponse[].class);
            return Arrays.stream(respons)
                .filter(response -> "AKTIV".equalsIgnoreCase(response.status()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IntegrasjonException("F-016913", String.format("NORG2 arbeidsfordeling feil ved oppslag mot %s", uri), e);
        }
    }

}
