package no.nav.vedtak.felles.integrasjon.rest;

import org.apache.http.impl.client.CloseableHttpClient;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.isso.SystemUserIdTokenProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og
 * callId
 *
 * @deprecated Erstattes av {@link OIDCTokenRequestFilter} som settes på
 *             {@link AbstractJerseyOidcRestClient}
 */
@Deprecated(since = "3.0.x", forRemoval = true)
public class OidcRestClient extends AbstractOidcRestClient {

    public OidcRestClient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    protected String getOIDCToken() {
        String oidcToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
        if (oidcToken != null) {
            return oidcToken;
        }

        var samlToken = SubjectHandler.getSubjectHandler().getSamlToken();
        if (samlToken != null) {
            return veksleSamlTokenTilOIDCToken(samlToken);
        }
        throw OidcRestClientFeil.FACTORY.klarteIkkeSkaffeOIDCToken().toException();
    }

    // TODO fra P2: Kalle STS for å veksle SAML til OIDC.
    // Gammel - bør heller sanere WS som tilbys
    private String veksleSamlTokenTilOIDCToken(@SuppressWarnings("unused") SAMLAssertionCredential samlToken) {
        return SystemUserIdTokenProvider.getSystemUserIdToken().getToken();
    }

}
