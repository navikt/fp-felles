package no.nav.vedtak.sikkerhet.oidc.token;

import java.net.URI;

import no.nav.vedtak.sikkerhet.context.containers.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.impl.AzureBrukerTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.AzureSystemTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.BrukerTokenProvider;
import no.nav.vedtak.sikkerhet.oidc.token.impl.StsSystemTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.TokenXExchangeKlient;

public final class TokenProvider {


    public static OpenIDToken getTokenFor(SikkerhetContext context) {
        return switch (context) {
            case BRUKER -> getTokenForBrukerMedFallbackDersomSaml(true);
            case SYSTEM -> getStsSystemToken();
        };
    }

    public static OpenIDToken getTokenUtenSamlFallback(SikkerhetContext context) {
        return switch (context) {
            case BRUKER -> getTokenForBrukerMedFallbackDersomSaml(false);
            case SYSTEM -> getStsSystemToken();
        };
    }

    public static OpenIDToken getTokenForScope(SikkerhetContext context, String scopes) {
        return switch (context) {
            case BRUKER -> getAzureTokenForBrukerMedFallbackDersomSaml(true, scopes);
            case SYSTEM -> getAzureSystemToken(scopes);
        };
    }

    public static String getUserIdFor(SikkerhetContext context) {
        return switch (context) {
            case BRUKER -> BrukerTokenProvider.getUserId();
            case SYSTEM -> ConfigProvider.getOpenIDConfiguration(OpenIDProvider.STS).map(OpenIDConfiguration::clientId).orElse(null);
        };
    }

    public static OpenIDToken getStsSystemToken() {
        return StsSystemTokenKlient.hentAccessToken();
    }

    public static OpenIDToken getAzureSystemToken(String scope) {
        return AzureSystemTokenKlient.instance().hentAccessToken(scope);
    }

    public static OpenIDToken exchangeTokenX(OpenIDToken token, String assertion, URI targetEndpoint) {
        // Assertion må være generert av den som skal bytte. Et JWT, RSA-signert, basert på injisert private jwk
        return TokenXExchangeKlient.instance().exchangeToken(token, assertion, targetEndpoint);
    }

    private static OpenIDToken getTokenForBrukerMedFallbackDersomSaml(boolean fallback) {
        if (!BrukerTokenProvider.harSattBrukerOidcToken() && BrukerTokenProvider.harSattBrukerSamlToken() && fallback) {
            return getStsSystemToken();
        }
        return BrukerTokenProvider.getToken();
    }

    private static OpenIDToken getAzureTokenForBrukerMedFallbackDersomSaml(boolean fallback, String scopes) {
        if (!BrukerTokenProvider.harSattBrukerOidcToken() && BrukerTokenProvider.harSattBrukerSamlToken() && fallback) {
            return getAzureSystemToken(scopes);
        }
        var token = BrukerTokenProvider.getToken();
        if (token != null && token.token() != null && OpenIDProvider.AZUREAD.equals(token.provider()) &&
            IdentType.InternBruker.equals(BrukerTokenProvider.getIdentType())) {
            return AzureBrukerTokenKlient.oboExchangeToken(token, scopes);
        }
        return token;
    }

}
