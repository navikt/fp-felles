package no.nav.vedtak.felles.integrasjon.sak.v1;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class LegacySakRestKlient implements SakClient {

    private static final String ENDPOINT_KEY = "sak.rs.url";
    private static final String DEFAULT_URI = "http://sak.default/api/v1/saker";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public LegacySakRestKlient() {
    }

    @Inject
    public LegacySakRestKlient(OidcRestClient oidcRestClient,
            @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpoint = endpoint;
    }

    @Override
    public SakJson opprettSak(SakJson sak) {
        return oidcRestClient.post(endpoint, sak, lagHeader(), SakJson.class);
    }

    @Override
    public Optional<SakJson> finnForSaksnummer(String saksnummer) throws Exception {
        URIBuilder builder = new URIBuilder(this.endpoint)
                .addParameter("fagsakNr", saksnummer);
        var match = this.oidcRestClient.get(builder.build(), lagHeader(), SakJson[].class);
        return Arrays.stream(match).findFirst();
    }

    @Override
    public SakJson hentSakId(String sakId) {
        var request = URI.create(endpoint.toString() + "/" + sakId);
        return oidcRestClient.get(request, lagHeader(), SakJson.class);
    }

    private static Set<Header> lagHeader() {
        return Collections.singleton(new BasicHeader(HEADER_CORRELATION_ID, MDCOperations.getCallId()));
    }

}
