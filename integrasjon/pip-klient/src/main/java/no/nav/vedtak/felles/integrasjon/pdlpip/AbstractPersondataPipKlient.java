package no.nav.vedtak.felles.integrasjon.pdlpip;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

/*
 * Informasjon fra PDL til bruk kun for tilgangskontroll. Registrer klientapps hos pdl
 *
 * PROD: SD innenfor FSS ellers https pdl-pip-api.intern.nav.no (scope: prod-fss:pdl:pdl-pip-api)
 * DEV: SD innenfor FSS ellers https pdl-pip-api.dev.intern.nav.no (scope: dev-fss:pdl:pdl-pip-api)
 */

// Extend og annoter med endpoint+default og scopes/default + tokenConfig = AzureAD_CC
//@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "pdl.pip.base.url",
//    endpointDefault = "http://pdl-pip-api.pdl/api/v1", // For FSS - apps i GCP bruker https://pdl-pip-api.intern.nav.no/api/v1
//    scopesProperty = "pdl.pip.scope", scopesDefault = "api://prod-fss:pdl:pdl-pip-api/.default")
public abstract class AbstractPersondataPipKlient implements PersondataPip {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPersondataPipKlient.class);
    private static final String PERSON_PATH = "person";
    private static final String PERSON_BOLK_PATH = "personBolk";

    private final RestClient client;
    private final RestConfig restConfig;
    private final URI pipPersonBolkEndpoint;
    private final URI pipPersonEndpoint;


    protected AbstractPersondataPipKlient() {
        this(RestClient.client());
    }

    protected AbstractPersondataPipKlient(RestClient restClient) {
        this.client = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.pipPersonEndpoint =  UriBuilder.fromUri(restConfig.endpoint()).path(PERSON_PATH).build();
        this.pipPersonBolkEndpoint =  UriBuilder.fromUri(restConfig.endpoint()).path(PERSON_BOLK_PATH).build();
        if (!restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Utviklerfeil: klient må annoteres med Azure CC");
        }
    }

    @Override
    public PersondataPipDto hentTilgangPersondata(String ident) {
        var request = RestRequest.newGET(pipPersonEndpoint, restConfig)
            .header("ident", ident)
            .timeout(Duration.ofSeconds(5));
        try {
            return client.send(request, PersondataPipDto.class);
        } catch (Exception e) {
            LOG.info("PdlPip fikk feil", e);
        }
        return client.send(request, PersondataPipDto.class);
    }

    @Override
    public Map<String, PersondataPipDto> hentTilgangPersondataBolk(List<String> identer) {
        var request = RestRequest.newPOSTJson(identer, pipPersonBolkEndpoint, restConfig)
            .timeout(Duration.ofSeconds(5));
        try {
            return client.sendReturnMap(request, PersondataPipDto.class);
        } catch (Exception e) {
            LOG.info("PdlPip fikk feil", e);
        }
        return client.sendReturnMap(request, PersondataPipDto.class);
    }

}
