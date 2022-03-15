package no.nav.vedtak.sikkerhet.oidc.token;

import java.util.Optional;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.impl.AzureSystemTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.BrukerTokenProvider;
import no.nav.vedtak.sikkerhet.oidc.token.impl.OpenAmBrukerTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.StsSystemTokenKlient;

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

    public static OpenIDToken exhangeOpenAmAuthCode(String authorizationCode, String callback) {
        return OpenAmBrukerTokenKlient.exhangeAuthCode(authorizationCode, callback);
    }

    public static Optional<OpenIDToken> refreshOpenAmIdToken(OpenIDToken expiredToken, String clientName) {
        return OpenAmBrukerTokenKlient.refreshIdToken(expiredToken, clientName);
    }

    public static OpenIDToken exchangeAzureOBO(String tokenToExchange, String scope) {
        // Senere: bytte OBO token
        throw new TekniskException("F-872314", "Azure exchange ikke implementert ennå");
    }

    public static OpenIDToken exchangeTokenX(String tokenToExchange, String audience) {
        // Senere: bytte OBO token - ta inn kode fra rest. Trenger å lande JWT-bibliotek
        throw new TekniskException("F-872314", "TokenX exchange ikke integrert ennå");
    }

    private static OpenIDToken getTokenForBrukerMedFallbackDersomSaml(boolean fallback) {
        if (!BrukerTokenProvider.harSattBrukerOidcToken() && BrukerTokenProvider.harSattBrukerSamlToken() && fallback) {
            return getStsSystemToken();
        }
        return BrukerTokenProvider.getToken();
    }

}
