package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static org.apache.commons.lang3.ArrayUtils.add;

import java.util.Arrays;

import javax.ws.rs.client.ClientRequestFilter;

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
        this(mapper, OidcTokenRequestFilter.class);
    }

    public AbstractJerseyOidcRestClient(Class<? extends ClientRequestFilter>... filters) {
        this(mapper, filters);
    }

    public AbstractJerseyOidcRestClient(ObjectMapper mapper, Class<? extends ClientRequestFilter>... filters) {
        super(mapper, addIfRequiredNotPresent(filters, OidcTokenRequestFilter.class));
    }

    public AbstractJerseyOidcRestClient(ClientRequestFilter... filters) {
        super(mapper, addIfRequiredNotPresent(filters, new OidcTokenRequestFilter()));
    }

    private static <T> T[] addIfRequiredNotPresent(final T[] filters, final T required) {
        return Arrays.stream(filters)
                .filter(e -> e.getClass().equals(required.getClass()))
                .findFirst()
                .map(m -> filters)
                .orElseGet(() -> add(filters, required));
    }
}
