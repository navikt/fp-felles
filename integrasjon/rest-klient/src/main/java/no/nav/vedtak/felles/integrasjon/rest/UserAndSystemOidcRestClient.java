package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

import no.nav.vedtak.isso.SystemUserIdTokenProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

/**
 * Blir til PGA PDL som skal ha både userToken og consumerToken - de kan være like eller ulike
 */
public class UserAndSystemOidcRestClient extends AbstractOidcRestClient {

    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    private static final String NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token";

    public UserAndSystemOidcRestClient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        request.setHeader(NAV_CONSUMER_TOKEN_HEADER, OIDC_AUTH_HEADER_PREFIX + systemUserOIDCToken());

        return super.doExecute(target, request, context);
    }


    @Override
    protected String getOIDCToken() {
        String oidcToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
        if (oidcToken != null) {
            return oidcToken;
        }

        var samlToken = SubjectHandler.getSubjectHandler().getSamlToken();
        if (samlToken != null) {
            // Arv fra P2: Kalle STS for å veksle SAML til OIDC. Eller heller sanere WS som tilbys
            return systemUserOIDCToken();
        }
        throw OidcRestClientFeil.FACTORY.klarteIkkeSkaffeOIDCToken().toException();
    }

    private String systemUserOIDCToken() {
        return SystemUserIdTokenProvider.getSystemUserIdToken().getToken();
    }
}
