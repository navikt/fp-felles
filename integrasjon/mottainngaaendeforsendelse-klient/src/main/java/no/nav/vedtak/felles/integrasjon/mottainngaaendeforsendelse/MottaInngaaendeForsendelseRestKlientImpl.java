package no.nav.vedtak.felles.integrasjon.mottainngaaendeforsendelse;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRequest;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseResponse;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class MottaInngaaendeForsendelseRestKlientImpl implements MottaInngaaendeForsendelseRestKlient {

    private static final String MOTTA_INNGAAENDE_FORSENDELSE_URL = "mottaInngaaendeForsendelse.url";

    private OidcRestClient restClient;
    private URI endpoint;

    @Inject
    public MottaInngaaendeForsendelseRestKlientImpl(OidcRestClient restClient, @KonfigVerdi(MOTTA_INNGAAENDE_FORSENDELSE_URL) URI endpoint) {
        this.restClient = restClient;
        this.endpoint = endpoint;
    }

    @Override
    public MottaInngaaendeForsendelseResponse journalførForsendelse(MottaInngaaendeForsendelseRequest request) {
        return restClient.post(endpoint, request, MottaInngaaendeForsendelseResponse.class);
    }
}
