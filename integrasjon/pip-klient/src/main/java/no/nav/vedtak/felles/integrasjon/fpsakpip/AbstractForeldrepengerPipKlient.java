package no.nav.vedtak.felles.integrasjon.fpsakpip;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

/*
 * Informasjon fra skjermingsløsningen til bruk for tilgangskontroll. Registrer klientapps hos nom
 */

// Extend og annoter med @RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPSAK)
public abstract class AbstractForeldrepengerPipKlient implements ForeldrepengerPip {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractForeldrepengerPipKlient.class);

    private static final String SAK_AKTØR_PATH = "/api/pip/aktoer-for-sak";

    private final RestClient restClient;
    private final RestConfig restConfig;

    private final URI sakAktørerEndpoint;

    protected AbstractForeldrepengerPipKlient() {
        this(RestClient.client());
    }

    protected AbstractForeldrepengerPipKlient(RestClient restClient) {
        this.restClient = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.sakAktørerEndpoint = UriBuilder.fromUri(restConfig.fpContextPath()).path(SAK_AKTØR_PATH).build();
        if (!restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Utviklerfeil: klient må annoteres med Azure CC");
        }
    }


    @Override
    public List<ForeldrepengerPipAktørId> personerForSak(String saksnummer) {
        if (saksnummer == null) {
            return List.of();
        }

        var request = RestRequest.newPOSTJson(new SaksnummerDto(saksnummer), sakAktørerEndpoint, restConfig);

        try {
            return restClient.sendReturnList(request, ForeldrepengerPipAktørId.class);
        } catch (Exception e) {
            LOG.info("ForeldrepengerPip fikk feil", e);
        }
        return restClient.sendReturnList(request, ForeldrepengerPipAktørId.class);
    }

    public record SaksnummerDto(@NotNull String saksnummer) {
        public SaksnummerDto {
            Objects.requireNonNull(saksnummer, "saksnummer");
        }
    }

}
