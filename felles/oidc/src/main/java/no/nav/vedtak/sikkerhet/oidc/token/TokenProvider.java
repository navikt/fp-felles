package no.nav.vedtak.sikkerhet.oidc.token;

import java.net.URI;
import java.util.Optional;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.impl.AzureBrukerTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.AzureSystemTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.BrukerTokenProvider;
import no.nav.vedtak.sikkerhet.oidc.token.impl.StsSystemTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.TokenXExchangeKlient;

public final class TokenProvider {

    private TokenProvider() {
    }

    public static OpenIDToken getTokenForSystem() {
        return getTokenForSystem(OpenIDProvider.STS, null);
    }

    public static OpenIDToken getTokenXUtenSamlFallback() {
        return getTokenFraContextFor(OpenIDProvider.TOKENX, null, false);
    }

    public static OpenIDToken getTokenForSystem(OpenIDProvider provider, String scopes) {
        return OpenIDProvider.AZUREAD.equals(provider) ? getAzureSystemToken(scopes) : getStsSystemToken();
    }

    public static OpenIDToken getTokenFromCurrent(SikkerhetContext context, String scopes) {
        if (!BrukerTokenProvider.harSattBrukerOidcToken() && BrukerTokenProvider.harSattBrukerSamlToken()) {
            return getStsSystemToken();
        }
        var token = BrukerTokenProvider.getToken();
        return switch (context) {
            case REQUEST -> getTokenFraContextFor(token, scopes);
            case SYSTEM -> OpenIDProvider.AZUREAD.equals(getProvider(token)) ? getAzureSystemToken(scopes) : getStsSystemToken();
            case WSREQUEST -> getStsSystemToken();
        };
    }

    public static String getUserIdFor(SikkerhetContext context) {
        return switch (context) {
            case REQUEST, WSREQUEST -> BrukerTokenProvider.getUserId();
            case SYSTEM -> ConfigProvider.getOpenIDConfiguration(OpenIDProvider.STS).map(OpenIDConfiguration::clientId).orElse(null);
        };
    }

    public static boolean isAzureContext() {
        try {
            return OpenIDProvider.AZUREAD.equals(getProvider(BrukerTokenProvider.getToken()));
        } catch (Exception e) {
            return false;
        }
    }

    private static OpenIDToken getStsSystemToken() {
        return StsSystemTokenKlient.hentAccessToken();
    }

    private static OpenIDToken getAzureSystemToken(String scopes) {
        return AzureSystemTokenKlient.instance().hentAccessToken(scopes);
    }

    private static OpenIDToken veksleAzureAccessToken(OpenIDToken incoming, String scopes) {
        var uid = BrukerTokenProvider.getUserId();
        return AzureBrukerTokenKlient.instance().oboExchangeToken(uid, incoming, scopes);
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

    private static OpenIDToken getTokenFraContextFor(OpenIDToken incoming, String scopes) {
        if (incoming == null || incoming.token() == null) {
            return incoming;
        }
        var providerIncoming = getProvider(incoming);
        var identType = Optional.ofNullable(BrukerTokenProvider.getIdentType()).orElse(IdentType.InternBruker);
        if (OpenIDProvider.AZUREAD.equals(providerIncoming)) {
            return identType.erSystem() ? getAzureSystemToken(scopes) : veksleAzureAccessToken(incoming, scopes);
        } else {
            return incoming;
        }
    }

    private static TekniskException ugyldigTokenCombo(OpenIDToken token, IdentType identType, OpenIDProvider provider) {
        return new TekniskException("F-483213", String.format("Ugyldig veksling, har token fra %s for %s. Trenger %s",
            token.provider(), identType, provider));
    }

    private static OpenIDProvider getProvider(OpenIDToken token) {
        return Optional.ofNullable(token).map(OpenIDToken::provider).orElse(null);
    }

}
