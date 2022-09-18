package no.nav.vedtak.sikkerhet.oidc.token;

import java.net.URI;
import java.util.Optional;

import no.nav.vedtak.exception.TekniskException;
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
            case BRUKER -> getTokenFraContextFor(OpenIDProvider.ISSO, null, true);
            case SYSTEM -> getStsSystemToken();
        };
    }

    public static OpenIDToken getTokenUtenSamlFallback(SikkerhetContext context) {
        return switch (context) {
            case BRUKER -> getTokenFraContextFor(OpenIDProvider.ISSO, null, false);
            case SYSTEM -> getStsSystemToken();
        };
    }

    public static OpenIDToken getTokenFor(SikkerhetContext context, OpenIDProvider provider, String scopes) {
        return switch (context) {
            case BRUKER -> getTokenFraContextFor(provider, scopes, true);
            case SYSTEM -> OpenIDProvider.AZUREAD.equals(provider) ? getAzureSystemToken(scopes) : getStsSystemToken();
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

    public static OpenIDToken getAzureSystemToken(String scopes) {
        return AzureSystemTokenKlient.instance().hentAccessToken(scopes);
    }

    public static OpenIDToken veksleAzureAccessToken(OpenIDToken incoming, String scopes) {
        return AzureBrukerTokenKlient.instance().oboExchangeToken(incoming, scopes);
    }

    public static OpenIDToken exchangeTokenX(OpenIDToken token, String assertion, URI targetEndpoint) {
        // Assertion må være generert av den som skal bytte. Et JWT, RSA-signert, basert på injisert private jwk
        return TokenXExchangeKlient.instance().exchangeToken(token, assertion, targetEndpoint);
    }

    private static OpenIDToken getTokenFraContextFor(OpenIDProvider provider, String scopes, boolean samlFallback) {
        if (!BrukerTokenProvider.harSattBrukerOidcToken() && BrukerTokenProvider.harSattBrukerSamlToken() && samlFallback) {
            return OpenIDProvider.AZUREAD.equals(provider) ? getAzureSystemToken(scopes) : getStsSystemToken();
        }
        var identType = Optional.ofNullable(BrukerTokenProvider.getIdentType()).orElse(IdentType.InternBruker);
        var token = BrukerTokenProvider.getToken();
        if (token == null || token.token() == null) {
            return token;
            //throw new IllegalStateException("Har ikke token i kontekst");
        }
        if (OpenIDProvider.AZUREAD.equals(provider)) {
            if (identType.erSystem()) {
                return getAzureSystemToken(scopes);
            } else if (OpenIDProvider.AZUREAD.equals(token.provider())) {
                return veksleAzureAccessToken(token, scopes);
            } else {
                throw ugyldigTokenCombo(token, identType, provider);
            }
        } else {
            if (OpenIDProvider.AZUREAD.equals(token.provider()) && identType.erSystem()) {
                return getStsSystemToken();
            } else if (OpenIDProvider.AZUREAD.equals(token.provider())) {
                throw ugyldigTokenCombo(token, identType, provider);
            } else {
                return token;
            }
        }
    }

    private static TekniskException ugyldigTokenCombo(OpenIDToken token, IdentType identType, OpenIDProvider provider) {
        return new TekniskException("F-483213", String.format("Ugyldig veksling, har token fra %s for %s. Trenger %s",
            token.provider(), identType, provider));
    }

}
