package no.nav.vedtak.felles.integrasjon.skjerming;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

// Extend og annoter med endpoint+default og scopes/default + tokenConfig = AzureAD_CC
//@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "skjermet.person.rs.url", endpointDefault = "https://skjermede-personer-pip.intern.nav.no/skjermet",
//    scopesProperty = "skjermet.person.rs.azure.scope", scopesDefault = "api://prod-gcp.nom.skjermede-personer-pip/.default")
public abstract class AbstractSkjermetPersonGCPKlient implements Skjerming {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSkjermetPersonGCPKlient.class);
    private static final boolean TESTENV = Environment.current().isLocal();

    private final RestClient client;
    private final RestConfig restConfig;

    protected AbstractSkjermetPersonGCPKlient() {
        this(RestClient.client());
    }

    protected AbstractSkjermetPersonGCPKlient(RestClient restClient) {
        this.client = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        if (!restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Utviklerfeil: klient må annoteres med Azure CC");
        }
    }


    @Override
    public boolean erSkjermet(String fnr) {
        if (TESTENV || fnr == null) return false;

        var request = RestRequest.newPOSTJson(new SkjermetRequestDto(fnr), restConfig.endpoint(), restConfig)
            .timeout(Duration.ofSeconds(30));

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

    private record SkjermetRequestDto(String personident) {
    }

}
