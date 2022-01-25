package no.nav.vedtak.felles.integrasjon.rest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.containers.SAMLAssertionCredential;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og
 * callId
 *
 * @deprecated Erstattes av {@link OIDCTokenRequestFilter} som settes på
 *             {@link AbstractJerseyOidcRestClient}
 */
@Deprecated(since = "3.0.x", forRemoval = true)
public class OidcRestClient extends AbstractOidcRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(OidcRestClient.class);

    public OidcRestClient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    protected String getOIDCToken() {
        String oidcToken = TokenProvider.getTokenFor(SikkerhetContext.BRUKER).token();
        if (oidcToken != null) {
            LOG.trace("Internal token OK");
            return oidcToken;
        }

        var samlToken = SubjectHandler.getSubjectHandler().getSamlToken();
        if (samlToken != null) {
            LOG.trace("SAML token OK");
            return veksleSamlTokenTilOIDCToken(samlToken);
        }
        throw new TekniskException("F-937072", "Klarte ikke å fremskaffe et OIDC token");
    }

    // TODO fra P2: Kalle STS for å veksle SAML til OIDC.
    // Gammel - bør heller sanere WS som tilbys
    private String veksleSamlTokenTilOIDCToken(@SuppressWarnings("unused") SAMLAssertionCredential samlToken) {
        return TokenProvider.getTokenFor(SikkerhetContext.SYSTEM).token();
    }

}
