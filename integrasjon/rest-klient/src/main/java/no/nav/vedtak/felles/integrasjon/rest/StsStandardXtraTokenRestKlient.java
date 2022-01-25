package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/*
 * Restklient som setter Authorization ut fra kontekst og legger på et STS Nav-Consumer-Token
 */
public class StsStandardXtraTokenRestKlient extends AbstractOidcRestClient {

    private static final String NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token";

    public StsStandardXtraTokenRestKlient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        request.setHeader(NAV_CONSUMER_TOKEN_HEADER, OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + systemUserOIDCToken());

        return super.doExecute(target, request, context);
    }

    @Override
    protected String getOIDCToken() {
        String oidcToken = TokenProvider.getTokenFor(SikkerhetContext.BRUKER).token();
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
        return TokenProvider.getStsSystemToken().token();
    }
}
