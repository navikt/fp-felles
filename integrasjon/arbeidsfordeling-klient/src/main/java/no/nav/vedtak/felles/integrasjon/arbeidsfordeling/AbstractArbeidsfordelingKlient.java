package no.nav.vedtak.felles.integrasjon.arbeidsfordeling;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

// Extend og annoter med endpoint + default, evt tokenConfig for å illustrere
//@RestClientConfig(tokenConfig = TokenFlow.NO_AUTH_NEEDED, endpointProperty = "arbeidsfordeling.rs.url", endpointDefault = "https://app.adeo.no/norg2/api/v1/arbeidsfordeling/enheter")
public abstract class AbstractArbeidsfordelingKlient implements Arbeidsfordeling {

    private static final String BEST_MATCH = "/bestmatch";

    private final RestClient restKlient;
    private final RestConfig restConfig;
    private final URI alleEnheterUri;
    private final URI besteEnhetUri;

    protected AbstractArbeidsfordelingKlient() {
        this(RestClient.client());
    }

    protected AbstractArbeidsfordelingKlient(RestClient client) {
        this.restKlient = client;
        this.restConfig = RestConfig.forClient(this.getClass());
        if (this.restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Arbeidsfordeling støtter ikke Azure");
        }
        this.alleEnheterUri = restConfig.endpoint();
        this.besteEnhetUri = URI.create(restConfig.endpoint() + BEST_MATCH);
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
            var restrequest = RestRequest.newPOSTJson(request, uri, restConfig);
            var respons = restKlient.send(restrequest, ArbeidsfordelingResponse[].class);
            return Arrays.stream(respons).filter(response -> "AKTIV".equalsIgnoreCase(response.status())).toList();
        } catch (Exception e) {
            throw new IntegrasjonException("F-016913", String.format("NORG2 arbeidsfordeling feil ved oppslag mot %s", uri), e);
        }
    }

}
