package no.nav.vedtak.felles.integrasjon.rest.jersey;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientFeil;
import no.nav.vedtak.isso.SystemUserIdTokenProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;

public class OidcRestJerseyClient extends AbstractJerseyRestClient {

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
