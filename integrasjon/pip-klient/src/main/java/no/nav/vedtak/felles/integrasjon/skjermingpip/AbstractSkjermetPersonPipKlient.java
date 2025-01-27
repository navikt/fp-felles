package no.nav.vedtak.felles.integrasjon.skjermingpip;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

/*
 * Informasjon fra skjermingsløsningen til bruk for tilgangskontroll. Registrer klientapps hos nom
 */

// Extend og annoter med endpoint+default og scopes/default + tokenConfig = AzureAD_CC
//@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "skjermet.person.pip.base.url",
//    endpointDefault = "https://skjermede-personer-pip.intern.nav.no", // For FSS - apps i GCP bruker http://skjermede-personer-pip.nom
//    scopesProperty = "skjermet.person.pip.scope", scopesDefault = "api://prod-gcp.nom.skjermede-personer-pip/.default")
public abstract class AbstractSkjermetPersonPipKlient implements SkjermetPersonPip {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSkjermetPersonPipKlient.class);

    private static final String SKJERMET_PATH = "skjermet";
    private static final String BULK_PATH = "skjermetBulk";

    private final RestClient client;
    private final RestConfig restConfig;
    private final URI bulkEndpoint;
    private final URI skjermetEndpoint;

    protected AbstractSkjermetPersonPipKlient() {
        this(RestClient.client());
    }

    protected AbstractSkjermetPersonPipKlient(RestClient restClient) {
        this.client = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.skjermetEndpoint =  UriBuilder.fromUri(restConfig.endpoint()).path(SKJERMET_PATH).build();
        this.bulkEndpoint =  UriBuilder.fromUri(restConfig.endpoint()).path(BULK_PATH).build();
        if (!restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Utviklerfeil: klient må annoteres med Azure CC");
        }
    }


    @Override
    public boolean erSkjermet(String fnr) {
        if (fnr == null) {
            return false;
        }

        var request = RestRequest.newPOSTJson(new SkjermetRequestDto(fnr), skjermetEndpoint, restConfig);

        try {
            return kallMedSjekk(request);
        } catch (Exception e) {
            LOG.info("SkjermetPerson fikk feil", e);
        }
        return kallMedSjekk(request);
    }

    private boolean kallMedSjekk(RestRequest request) {
        var skjermet = client.send(request, String.class);
        return "true".equalsIgnoreCase(skjermet);
    }

    @Override
    public Map<String, Boolean> erSkjermet(List<String> fnr) {
        if (fnr == null || fnr.isEmpty()) {
            return Map.of();
        }

        var request = RestRequest.newPOSTJson(new SkjermetBulkRequestDto(fnr), bulkEndpoint, restConfig);

        try {
            return kallBulk(request);
        } catch (Exception e) {
            LOG.info("SkjermetPerson fikk feil", e);
        }
        return kallBulk(request);
    }

    @Override
    public boolean erNoenSkjermet(List<String> fnr) {
        if (fnr == null || fnr.isEmpty()) {
            return false;
        }
        return erSkjermet(fnr).values().stream().filter(Objects::nonNull).anyMatch(v -> v);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> kallBulk(RestRequest request) {
        // Se github / skjerming / PipController
        return client.send(request, Map.class);
    }

    private record SkjermetRequestDto(String personident) { }

    private record SkjermetBulkRequestDto(List<String> personidenter) { }

}
