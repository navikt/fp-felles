package no.nav.vedtak.sikkerhet.tilgang;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.klient.http.DefaultHttpClient;
import no.nav.vedtak.klient.http.HttpClientRequest;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/*
 * Informasjon fra PDL til bruk kun for tilgangskontroll
 *
 * PROD: SD innenfor FSS ellers https pdl-pip-api.intern.nav.no (scope: prod-fss:pdl:pdl-pip-api)
 * DEV: SD innenfor FSS ellers https pdl-pip-api.dev.intern.nav.no (scope: dev-fss:pdl:pdl-pip-api)
 */
@ApplicationScoped
public class TilgangPersondataKlient implements TilgangPersondata {

    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";

    private static final String BOLK_SUFFIX = "Bolk";

    private URI personURI;
    private URI personBolkURI;
    private String personScopes;


    TilgangPersondataKlient() {
    } // CDI

    @Inject
    public TilgangPersondataKlient(@KonfigVerdi(value = "pdl.pip.endpoint.url", defaultVerdi = "http://pdl-pip-api.pdll/api/v1/person") String pdlPipUrl,
                                   @KonfigVerdi(value = "pdl.pip.scopes", defaultVerdi = "api://prod-fss:pdl:pdl-pip-api/.default") String pdlPipScopes) {
        this.personURI = URI.create(pdlPipUrl);
        this.personBolkURI = URI.create(pdlPipUrl + BOLK_SUFFIX);
        this.personScopes = pdlPipScopes;
    }

    @Override
    public TilgangPersondataDto hentTilgangPersondata(String ident) {
        var builder = HttpRequest.newBuilder(personURI)
            .header(HttpHeaders.ACCEPT, MediaType.WILDCARD) // Bruk APPLICATION_JSON ?
            .header(HttpHeaders.AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + TokenProvider.getTokenForSystem(OpenIDProvider.AZUREAD, personScopes).token())
            .header("ident", ident)
            .timeout(Duration.ofSeconds(5))
            .GET();
        var request = new PersondataRequest(builder);

        var response = DefaultHttpClient.client().send(request);
        return response != null ? DefaultJsonMapper.fromJson(response, TilgangPersondataDto.class) : null;
    }

    @Override
    public Map<String, TilgangPersondataDto> hentTilgangPersondataBolk(List<String> identer) {
        var builder = HttpRequest.newBuilder(personBolkURI)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.WILDCARD) // Bruk APPLICATION_JSON ?
            .header(HttpHeaders.AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + TokenProvider.getTokenForSystem(OpenIDProvider.AZUREAD, personScopes).token())
            .timeout(Duration.ofSeconds(5))
            .POST(HttpRequest.BodyPublishers.ofString(DefaultJsonMapper.toJson(identer)));
        var request = new PersondataRequest(builder);

        var response = DefaultHttpClient.client().send(request);
        return response != null ? DefaultJsonMapper.mapFromJson(response, TilgangPersondataDto.class) : Map.of();
    }


    private static class PersondataRequest extends HttpClientRequest {
        public PersondataRequest(HttpRequest.Builder builder) {
            super(builder, Map.of());
        }
    }
}
