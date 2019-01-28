package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.Element;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og callId
 */
public class OidcRestClient extends AbstractOidcRestClient {

    public OidcRestClient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    String getOIDCToken() {
        String oidcToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
        if (oidcToken != null) {
            return oidcToken;
        }

        Element samlToken = SubjectHandler.getSubjectHandler().getSamlToken();
        if (samlToken != null) {
            return veksleSamlTokenTilOIDCToken(samlToken);
        }
        throw OidcRestClientFeil.FACTORY.klarteIkkeSkaffeOIDCToken().toException();
    }

    //FIXME (u139158): PK-50281 STS for SAML til OIDC
    // I mellomtiden bruker vi systemets OIDC-token, dvs vi propagerer ikke sikkerhetskonteksten
    private String veksleSamlTokenTilOIDCToken(Element samlToken) {
        try {
            return new OpenAMHelper().getToken().getIdToken().getToken();
        } catch (IOException e) {
            throw OidcRestClientFeil.FACTORY.feilVedHentingAvSystemToken(e).toException();
        }
    }

    interface OidcRestClientFeil extends DeklarerteFeil {

        OidcRestClientFeil FACTORY = FeilFactory.create(OidcRestClientFeil.class);

        @TekniskFeil(feilkode = "F-891590", feilmelding = "IOException ved henting av systemets OIDC-token", logLevel = LogLevel.ERROR)
        Feil feilVedHentingAvSystemToken(IOException cause);

        @TekniskFeil(feilkode = "F-937072", feilmelding = "Klarte ikke å fremskaffe et OIDC token", logLevel = LogLevel.ERROR)
        Feil klarteIkkeSkaffeOIDCToken();

    }

}