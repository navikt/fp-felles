package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

import java.net.URI;

import javax.ws.rs.client.ClientRequestFilter;

import org.apache.http.message.BasicHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.felles.integrasjon.rest.AbstractOidcRestClient;

/**
 * Denne klassen erstatter {@link AbstractOidcRestClient} og er ment som en
 * felles superklasse for alle tjenster som trenger et OIDC bearer token i en
 * AUTHORIZATION header. Dette OIDC-tokenet slås opp og settes av
 * {@link OidcTokenRequestFilter}
 *
 *
 * Subklasser kan konstrueres på 4 måter, med økende grad av fleksibilitet:
 *
 * <pre>
 * MyRestService extends AbstractJerseyOidcRestClient...
 * var myService = new MyRestService()                // benytter innebygget mapper og ett filter av type {@link OidcTokenRequestFilter}
 * var myService = new MyRestService(mapper)          // benytter custom mapper og ett filter av type {@link OidcTokenRequestFilter}
 * var myService = new MyRestService(filters)         // benytter innebygd mapper og et sett av  filters. Et filter av type {@link OidcTokenRequestFilter} blir alltid lagt til i tillegg
 * var myService = new MyRestService(mapper, filters) // benytter custom mapper og et sett av  filters. Et filter av type {@link OidcTokenRequestFilter} blir alltid lagt til i tillegg
 * </pre>
 *
 * Typisk bruk vil da være
 *
 * <pre>
 * public Sak hentSak(String id) {
        return client.target(endpoint)
                .path(id)
                .request(APPLICATION_JSON_TYPE)
                .get(Sak.class);
 * </pre>
 *
 */
public abstract class AbstractJerseyOidcRestClient extends AbstractJerseyRestClient {

    public AbstractJerseyOidcRestClient() {
        this(mapper);
    }

    public AbstractJerseyOidcRestClient(ObjectMapper mapper) {
        this(mapper, new OidcTokenRequestFilter());
    }

    public AbstractJerseyOidcRestClient(ClientRequestFilter... filters) {
        super(mapper, addIfRequiredNotPresent(filters, new OidcTokenRequestFilter()));
    }

    public AbstractJerseyOidcRestClient(ObjectMapper mapper, ClientRequestFilter... filters) {
        super(mapper, addIfRequiredNotPresent(filters, new OidcTokenRequestFilter()));
    }

    protected String patch(URI endpoint, Object obj) {
        return patch(endpoint, obj, new BasicHeader(ACCEPT, APPLICATION_JSON),
                new BasicHeader(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + new OidcTokenRequestFilter().accessToken()));
    }
}
