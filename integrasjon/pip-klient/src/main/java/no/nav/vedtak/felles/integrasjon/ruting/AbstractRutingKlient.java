package no.nav.vedtak.felles.integrasjon.ruting;

import jakarta.validation.Valid;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

import java.net.URI;
import java.util.Set;

// @RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPTILGANG) // Lokalt
public abstract class AbstractRutingKlient implements Ruting {

    private final URI rutingUri;
    private final RestClient klient;
    private final RestConfig restConfig;

    protected AbstractRutingKlient() {
        this(RestClient.client());
    }

    protected AbstractRutingKlient(RestClient restClient) {
        this.klient = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.rutingUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/ruting/egenskaper")
            .build();
        if (!restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Utviklerfeil: klient må annoteres med Azure CC");
        }
    }

    @Override
    public Set<RutingResultat> finnRutingEgenskaper(Set<String> aktørIdenter) {
        var request = new RutingRequest(aktørIdenter);
        var rrequest = RestRequest.newPOSTJson(request, rutingUri, restConfig);
        return klient.sendReturnOptional(rrequest, RutingRespons.class).map(RutingRespons::resultater).orElseGet(Set::of);
    }


    public record RutingRequest(@Valid Set<String> aktørIdenter) { }

    public record RutingRespons(Set<RutingResultat> resultater) { }

}
