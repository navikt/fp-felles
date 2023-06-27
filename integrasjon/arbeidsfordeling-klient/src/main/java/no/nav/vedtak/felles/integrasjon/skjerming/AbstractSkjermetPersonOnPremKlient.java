package no.nav.vedtak.felles.integrasjon.skjerming;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

// OBS på propertynavn vs GCP-versjon skjermet.person.onprem.rs.url=http://skjermede-personer-pip.nom/skjermet
// Extend og annoter med endpoint+default  + tokenConfig = STS_CC
//@RestClientConfig(tokenConfig = TokenFlow.NO_AUTH_NEEDED, endpointProperty = "skjermet.person.onprem.rs.url", endpointDefault = "http://skjermede-personer-pip.nom/skjermet"
public abstract class AbstractSkjermetPersonOnPremKlient implements Skjerming {

    private static final boolean TESTENV = Environment.current().isLocal();

    private static final String SKJERMET_PATH = "skjermet";
    private static final String BULK_PATH = "skjermetBulk";

    private final RestClient client;
    private final RestConfig restConfig;
    private final URI bulkEndpoint;
    private final URI skjermetEndpoint;

    protected AbstractSkjermetPersonOnPremKlient() {
        this(RestClient.client());
    }

    protected AbstractSkjermetPersonOnPremKlient(RestClient restClient) {
        this.client = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.skjermetEndpoint =  UriBuilder.fromUri(restConfig.endpoint()).path(SKJERMET_PATH).build();
        this.bulkEndpoint =  UriBuilder.fromUri(restConfig.endpoint()).path(BULK_PATH).build();
    }


    @Override
    public boolean erSkjermet(String fnr) {
        if (TESTENV || fnr == null) {
            return false;
        }

        var request = RestRequest.newPOSTJson(new SkjermetRequestDto(fnr), skjermetEndpoint, restConfig);

        var skjermet = client.send(request, String.class);
        return "true".equalsIgnoreCase(skjermet);
    }

    @Override
    public boolean erNoenSkjermet(List<String> fnr) {
        if (TESTENV || fnr == null || fnr.isEmpty()) {
            return false;
        }

        var request = RestRequest.newPOSTJson(new SkjermetBulkRequestDto(fnr), bulkEndpoint, restConfig);

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
