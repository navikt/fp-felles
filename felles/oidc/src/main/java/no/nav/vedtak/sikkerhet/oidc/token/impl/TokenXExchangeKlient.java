package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public final class TokenXExchangeKlient {

    private static final Environment ENV = Environment.current();
    private static final String CLUSTER = ENV.getCluster().clusterName();
    private static final String NAMESPACE = ENV.getNamespace().getName();

    private static final Logger LOG = LoggerFactory.getLogger(TokenXExchangeKlient.class);

    private static final String CLIENT_ID = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.TOKENX)
        .map(OpenIDConfiguration::clientId).orElse(null);
    private static final URI TOKEN_ENDPOINT = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.TOKENX)
        .map(OpenIDConfiguration::tokenEndpoint).orElse(null);

    private TokenXExchangeKlient() {
        // NOSONAR
    }

    public static OpenIDToken exchangeToken(OpenIDToken token, String assertion, URI targetEndpoint) {
        var audience = audience(targetEndpoint);
        var response = hentToken(token, assertion, audience);
        LOG.info("TokenX byttet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        return new OpenIDToken(OpenIDProvider.TOKENX, response.token_type(),
            new TokenString(response.access_token()), audience, response.expires_in());
    }

    private static OidcTokenResponse hentToken(OpenIDToken token, String assertion, String audience) {
        var request = HttpRequest.newBuilder()
            .header("Nav-Consumer-Id", CLIENT_ID)
            .header("Nav-Call-Id", MDCOperations.getCallId())
            .header("Cache-Control", "no-cache")
            .header("Content-type", "application/x-www-form-urlencoded")
            .timeout(Duration.ofSeconds(10))
            .uri(TOKEN_ENDPOINT)
            .POST(ofFormData(token, assertion, audience))
            .build();
        return GeneriskTokenKlient.hentToken(request, null);
    }

    private static HttpRequest.BodyPublisher ofFormData(OpenIDToken token, String assertion, String audience) {
        var formdata = "grant_type=urn:ietf:params:oauth:grant-type:token-exchange&" +
            "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&" +
            "client_assertion=" + assertion + "&" +
            "subject_token_type=urn:ietf:params:oauth:token-type:jwt&" +
            "subject_token=" + token.token() + "&" +
            "audience=" + audience;
        return HttpRequest.BodyPublishers.ofString(formdata, UTF_8);
    }

    private static String audience(URI uri) {
        String host = uri.getHost();
        var elems = host.split("\\.");
        var joiner = new StringJoiner(":");
        joiner.add(CLUSTER);

        if (elems.length == 1) {
            joiner.add(NAMESPACE);
            joiner.add(elems[0]);
            return joiner.toString();
        }
        if (elems.length == 2) {
            joiner.add(elems[1]);
            joiner.add(elems[0]);
            return joiner.toString();
        }
        throw new IllegalArgumentException("Kan ikke analysere " + host + "(" + elems.length + ")");
    }
}
