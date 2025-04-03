package no.nav.vedtak.felles.integrasjon.fpsakpip;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    private final RestClient restClient;
    private final RestConfig restConfig;

    private final URI sakAktørerEndpoint;
    private final URI behSaksnummerEndpoint;
    private final URI behSakAktørerEndpoint;

    protected AbstractForeldrepengerPipKlient() {
        this(RestClient.client());
    }

    protected AbstractForeldrepengerPipKlient(RestClient restClient) {
        this.restClient = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.sakAktørerEndpoint = UriBuilder.fromUri(restConfig.fpContextPath()).path("/api/pip/aktoer-for-sak").build();
        this.behSaksnummerEndpoint = UriBuilder.fromUri(restConfig.fpContextPath()).path("/api/pip/saksnummer-for-behandling").build();
        this.behSakAktørerEndpoint = UriBuilder.fromUri(restConfig.fpContextPath()).path("/api/pip/sak-aktoer-for-behandling").build();
        if (!restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Utviklerfeil: klient må annoteres med Azure CC");
        }
    }


    @Override
    public List<String> personerForSak(String saksnummer) {
        if (saksnummer == null) {
            return List.of();
        }

        var uri = UriBuilder.fromUri(sakAktørerEndpoint)
            .queryParam("saksnummer", saksnummer)
            .build();
        var request = RestRequest.newGET(uri, restConfig);

        try {
            return restClient.sendReturnList(request, String.class);
        } catch (Exception e) {
            LOG.info("ForeldrepengerPip personForSak fikk feil", e);
        }
        return restClient.sendReturnList(request, String.class);
    }

    @Override
    public List<SakMedPersonerDto> personerForSaker(Set<String> saksnummer) {
        if (saksnummer == null || saksnummer.isEmpty()) {
            return List.of();
        }

        var request = RestRequest.newPOSTJson(saksnummer, sakAktørerEndpoint, restConfig);

        try {
            return restClient.sendReturnList(request, SakMedPersonerDto.class);
        } catch (Exception e) {
            LOG.info("ForeldrepengerPip personerForSaker fikk feil", e);
        }
        return restClient.sendReturnList(request, SakMedPersonerDto.class);
    }

    @Override
    public String saksnummerForBehandling(UUID behandlingUuid) {
        if (behandlingUuid == null) {
            return null;
        }

        var uri = UriBuilder.fromUri(behSaksnummerEndpoint)
            .queryParam("behandlingUuid", behandlingUuid.toString())
            .build();
        var request = RestRequest.newGET(uri, restConfig);

        try {
            return restClient.send(request, String.class);
        } catch (Exception e) {
            LOG.info("ForeldrepengerPip sakForBehandling fikk feil", e);
        }
        return restClient.send(request, String.class);
    }

    @Override
    public SakMedPersonerDto sakPersonerForBehandling(UUID behandlingUuid) {
        if (behandlingUuid == null) {
            return null;
        }

        var uri = UriBuilder.fromUri(behSakAktørerEndpoint)
            .queryParam("behandlingUuid", behandlingUuid.toString())
            .build();
        var request = RestRequest.newGET(uri, restConfig);

        try {
            return restClient.send(request, SakMedPersonerDto.class);
        } catch (Exception e) {
            LOG.info("ForeldrepengerPip sakPersonerForBehandling fikk feil", e);
        }
        return restClient.send(request, SakMedPersonerDto.class);
    }

}
