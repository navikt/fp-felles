package no.nav.vedtak.felles.integrasjon.rest;

import org.apache.http.impl.client.CloseableHttpClient;

import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/**
 *
 * @deprecated Erstattes av {@link SystemUserOIDCTokenRequestFilter}
 *
 *             IKKE BRUK - MED MINDRE DU VET EKSAKT HVA DU GJØR.
 *             <p>
 *             Du skal mest sannsynlig bruke OidcRestClient direkte.
 *             <p>
 *             Denne gjør alle kall med systembrukerens sikkerhetskontekst
 *
 *
 */
@Deprecated(since = "3.0.x", forRemoval = true)
public class SystemUserOidcRestClient extends AbstractOidcRestClient {
    public SystemUserOidcRestClient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    String getOIDCToken() {
        return TokenProvider.getTokenFor(SikkerhetContext.SYSTEM).token();
    }
}
