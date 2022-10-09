package no.nav.vedtak.felles.integrasjon.skjerming;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

// OBS på propertynavn vs GCP-versjon skjermet.person.onprem.rs.url=http://skjermede-personer-pip.nom/skjermet
// Extend og annoter med endpoint+default  + tokenConfig = STS_CC
//@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "skjermet.person.onprem.rs.url", endpointDefault = "http://skjermede-personer-pip.nom/skjermet"
public abstract class AbstractSkjermetPersonOnPremKlient implements Skjerming {

    private static final boolean TESTENV = Environment.current().isLocal();

    private final RestClient client;
    private final RestConfig restConfig;

    protected AbstractSkjermetPersonOnPremKlient() {
        this(RestClient.client());
    }

    protected AbstractSkjermetPersonOnPremKlient(RestClient restClient) {
        this.client = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
    }


    @Override
    public boolean erSkjermet(String fnr) {
        if (TESTENV || fnr == null) return false;

        var request = RestRequest.newPOSTJson(new SkjermetRequestDto(fnr), restConfig.endpoint(), restConfig);

        var skjermet = client.send(request, String.class);
        return "true".equalsIgnoreCase(skjermet);
    }

    private boolean kallMedSjekk(RestRequest request) {
        var skjermet = client.send(request, String.class);
        return "true".equalsIgnoreCase(skjermet);
    }

    private record SkjermetRequestDto(String personident) {}

}
