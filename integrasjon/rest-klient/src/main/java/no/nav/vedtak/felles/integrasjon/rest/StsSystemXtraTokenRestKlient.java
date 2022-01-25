package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/*
 * Restklient som setter Authorization ut fra kontekst og legger på et STS Nav-Consumer-Token
 */
public class StsSystemXtraTokenRestKlient extends AbstractOidcRestClient {

    private static final String NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token";

    public StsSystemXtraTokenRestKlient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        request.setHeader(NAV_CONSUMER_TOKEN_HEADER, OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + systemUserOIDCToken());

        return super.doExecute(target, request, context);
    }

    @Override
    String getOIDCToken() {
        return systemUserOIDCToken();
    }


    private String systemUserOIDCToken() {
        return TokenProvider.getStsSystemToken().token();
    }
}
