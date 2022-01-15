package no.nav.vedtak.felles.integrasjon.rest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.AbstractOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.tokenhenter.StsAccessTokenKlient;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og callId
 *
 */
public class StsStandardRestKlient extends AbstractOidcRestClient {

    public StsStandardRestKlient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    protected String getOIDCToken() {
        String oidcToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
        if (oidcToken != null) {
            return oidcToken;
        }

        // Leftover P2 - vil sanere WS+SAML som tilbys
        var samlToken = SubjectHandler.getSubjectHandler().getSamlToken();
        if (samlToken != null) {
            return systemUserOIDCToken();
        }
        throw new TekniskException("F-937072", "Klarte ikke å fremskaffe et OIDC token");
    }

    private String systemUserOIDCToken() {
        return StsAccessTokenKlient.hentAccessToken();
    }

}
