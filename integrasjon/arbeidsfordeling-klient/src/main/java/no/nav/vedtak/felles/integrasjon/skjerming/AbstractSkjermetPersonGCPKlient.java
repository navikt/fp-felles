package no.nav.vedtak.felles.integrasjon.skjerming;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

// Extend og annoter med endpoint+default og scopes/default + tokenConfig = AzureAD_CC
//@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "skjermet.person.rs.url", endpointDefault = "https://skjermede-personer-pip.intern.nav.no/skjermet",
//    scopesProperty = "skjermet.person.rs.azure.scope", scopesDefault = "api://prod-gcp.nom.skjermede-personer-pip/.default")
public abstract class AbstractSkjermetPersonGCPKlient implements Skjerming {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSkjermetPersonGCPKlient.class);
    private static final boolean TESTENV = Environment.current().isLocal();

    private static final String SKJERMET_PATH = "skjermet";
    private static final String BULK_PATH = "skjermetBulk";

    private final RestClient client;
    private final RestConfig restConfig;
    private final URI bulkEndpoint;
    private final URI skjermetEndpoint;

    protected AbstractSkjermetPersonGCPKlient() {
        this(RestClient.client());
    }

    protected AbstractSkjermetPersonGCPKlient(RestClient restClient) {
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
        if (TESTENV || fnr == null) {
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
    public boolean erNoenSkjermet(List<String> fnr) {
        if (TESTENV || fnr == null || fnr.isEmpty()) {
            return false;
        }

        var request = RestRequest.newPOSTJson(new SkjermetBulkRequestDto(fnr), bulkEndpoint, restConfig);

        try {
            return kallBulkMedSjekk(request);
        } catch (Exception e) {
            LOG.info("SkjermetPerson fikk feil", e);
        }
        return kallBulkMedSjekk(request);
    }

    @SuppressWarnings("unchecked")
    private boolean kallBulkMedSjekk(RestRequest request) {
        // Se github / skjerming / PipController
        Map<String, Boolean> skjermet = client.send(request, Map.class);
        return skjermet.values().stream().anyMatch(v -> v);
    }

    private record SkjermetRequestDto(String personident) { }

    private record SkjermetBulkRequestDto(List<String> personidenter) { }

}
