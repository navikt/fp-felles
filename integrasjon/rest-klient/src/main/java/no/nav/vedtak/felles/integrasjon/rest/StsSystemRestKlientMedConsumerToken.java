package no.nav.vedtak.felles.integrasjon.rest;

import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createHttpClient;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.protocol.HttpContext;

import no.nav.vedtak.felles.integrasjon.rest.AbstractOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.tokenhenter.StsAccessTokenKlient;

/*
 * Restklient som setter Authorization ut fra kontekst og legger på et STS Nav-Consumer-Token
 */
public class StsSystemRestKlientMedConsumerToken extends AbstractOidcRestClient {

    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    private static final String NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token";

    public StsSystemRestKlientMedConsumerToken(StsAccessTokenConfig config) {
        super(createHttpClient());
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        request.setHeader(NAV_CONSUMER_TOKEN_HEADER, OIDC_AUTH_HEADER_PREFIX + systemUserOIDCToken());

        return super.doExecute(target, request, context);
    }

    @Override
    String getOIDCToken() {
        return systemUserOIDCToken();
    }


    private String systemUserOIDCToken() {
        return StsAccessTokenKlient.hentAccessToken();
    }
}
