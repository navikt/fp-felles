package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.Element;

import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og callId
 */
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

    //FIXME (u139158): PK-50281 STS for SAML til OIDC
    // I mellomtiden bruker vi systemets OIDC-token, dvs vi propagerer ikke sikkerhetskonteksten
    private String veksleSamlTokenTilOIDCToken(@SuppressWarnings("unused") SAMLAssertionCredential samlToken) {
        try {
            return new OpenAMHelper().getToken().getIdToken().getToken();
        } catch (IOException e) {
            throw OidcRestClientFeil.FACTORY.feilVedHentingAvSystemToken(e).toException();
        }
    }

}
