package no.nav.vedtak.felles.integrasjon.tilgangfilter;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

// @RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPTILGANG) // Lokalt
public abstract class AbstractTilgangFilterKlient implements TilgangFilter {

    private final URI filterIdenterUri;
    private final URI filterSaksnummerUri;
    private final RestClient klient;
    private final RestConfig restConfig;

    protected AbstractTilgangFilterKlient() {
        this(RestClient.client());
    }

    protected AbstractTilgangFilterKlient(RestClient restClient) {
        this.klient = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.filterIdenterUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/populasjon/filteridenter")
            .build();
        this.filterSaksnummerUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/populasjon/filtersaksnummer")
            .build();
        if (!restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Utviklerfeil: klient må annoteres med Azure CC");
        }
    }

    @Override
    public Set<String> filterSaksnummer(UUID ansattOid, Set<String> saksnummer) {
        var request = new FilterSaksnummerRequest(ansattOid, saksnummer);
        var rrequest = RestRequest.newPOSTJson(request, filterSaksnummerUri, restConfig);
        return klient.sendReturnOptional(rrequest, FilterResponse.class).map(FilterResponse::harTilgang).orElseGet(Set::of);
    }

    @Override
    public Set<String> filterIdenter(UUID ansattOid, Set<String> identer) {
        var request = new FilterIdenterRequest(ansattOid, identer);
        var rrequest = RestRequest.newPOSTJson(request, filterIdenterUri, restConfig);
        return klient.sendReturnOptional(rrequest, FilterResponse.class).map(FilterResponse::harTilgang).orElseGet(Set::of);
    }


    // For å sjekke hvilke saker den ansatte har tilgang til basert på saksnummer
    private record FilterSaksnummerRequest(@NotNull UUID ansattOid, Set<String> saker) { }

    // For å sjekke hvilke identer den ansatte har tilgang til basert på identer
    private record FilterIdenterRequest(@NotNull UUID ansattOid, Set<String> identer) { }

    // Hvilke av sakene/identene i request som den ansatte har tilgang til
    private record FilterResponse(Set<String> harTilgang) { }

}
