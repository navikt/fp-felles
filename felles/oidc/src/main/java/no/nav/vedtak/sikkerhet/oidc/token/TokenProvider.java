package no.nav.vedtak.sikkerhet.oidc.token;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.impl.AzureSystemTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.BrukerTokenProvider;
import no.nav.vedtak.sikkerhet.oidc.token.impl.StsSystemTokenKlient;

public final class TokenProvider {


    public static OpenIDToken getTokenFor(SikkerhetContext context) {
        return switch (context) {
            case BRUKER -> BrukerTokenProvider.getToken();
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

    public static OpenIDToken exchangeAzureOBO(String tokenToExchange, String scope) {
        // Senere: bytte OBO token
        throw new TekniskException("F-872314", "Azure exchange ikke implementert ennå");
    }

    public static OpenIDToken exchangeTokenX(String tokenToExchange, String audience) {
        // Senere: bytte OBO token - ta inn kode fra rest. Trenger å lande JWT-bibliotek
        throw new TekniskException("F-872314", "TokenX exchange ikke integrert ennå");
    }

}
