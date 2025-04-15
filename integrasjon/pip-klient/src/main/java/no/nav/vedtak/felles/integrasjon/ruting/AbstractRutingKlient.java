package no.nav.vedtak.felles.integrasjon.ruting;

import java.net.URI;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

// @RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPTILGANG) // Lokalt
public abstract class AbstractRutingKlient implements Ruting {

    private final URI rutingIdenterUri;
    private final URI rutingSakUri;
    private final RestClient klient;
    private final RestConfig restConfig;

    protected AbstractRutingKlient() {
        this(RestClient.client());
    }

    protected AbstractRutingKlient(RestClient restClient) {
        this.klient = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.rutingIdenterUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/ruting/identer")
            .build();
        this.rutingSakUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/ruting/sak")
            .build();
        if (!restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Utviklerfeil: klient må annoteres med Azure CC");
        }
    }

    @Override
    public Set<RutingResultat> finnRutingEgenskaper(Set<String> identer) {
        var request = new RutingIdenterRequest(identer);
        var rrequest = RestRequest.newPOSTJson(request, rutingIdenterUri, restConfig);
        return klient.sendReturnOptional(rrequest, RutingRespons.class).map(RutingRespons::resultater).orElseGet(Set::of);
    }

    @Override
    public Set<RutingResultat> finnRutingEgenskaper(String saksnummer) {
        var request = new RutingSakRequest(saksnummer);
        var rrequest = RestRequest.newPOSTJson(request, rutingSakUri, restConfig);
        return klient.sendReturnOptional(rrequest, RutingRespons.class).map(RutingRespons::resultater).orElseGet(Set::of);
    }


    public record RutingIdenterRequest(@Valid Set<String> identer) { }

    public record RutingSakRequest(@Valid @NotNull String saksnummer) { }

    public record RutingRespons(Set<RutingResultat> resultater) { }

}
